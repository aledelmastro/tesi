package org.opentripplanner.api.resource;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.geotools.geometry.Envelope2D;
import org.json.simple.JSONObject;
import org.locationtech.jts.geom.Envelope;
import org.opentripplanner.api.parameter.MIMEImageFormat;
import org.opentripplanner.common.geometry.MapTile;
import org.opentripplanner.common.geometry.WebMercatorTile;
import org.opentripplanner.ext.greenrouting.GreenRouting;
import org.opentripplanner.ext.greenrouting.configuration.GreenRoutingConfig;
import org.opentripplanner.ext.greenrouting.edgetype.GreenStreetEdge;
import org.opentripplanner.inspector.TileRenderer;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.standalone.server.OTPServer;
import org.opentripplanner.standalone.server.Router;

/**
 * Slippy map tile API for rendering various graph information for inspection/debugging purpose
 * (bike safety factor, connectivity...).
 * <p>
 * One can easily add a new layer by adding the following kind of code to a leaflet map:
 *
 * <pre>
 *   var bikesafety = new L.TileLayer(
 *      'http://localhost:8080/otp/routers/default/inspector/tile/bike-safety/{z}/{x}/{y}.png',
 *      { maxZoom : 22 });
 *   var map = L.map(...);
 *   L.control.layers(null, { "Bike safety": bikesafety }).addTo(map);
 * </pre>
 * <p>
 * Tile rendering goes through TileRendererManager which select the appropriate renderer for the
 * given layer.
 *
 * @author laurent
 * @see org.opentripplanner.inspector.TileRendererManager
 * @see TileRenderer
 */
@Path("/routers/{ignoreRouterId}/inspector")
public class GraphInspectorTileResource {

    @QueryParam("latTl")
    double latTl;
    @QueryParam("lngTl")
    double lngTl;
    @QueryParam("latBr")
    double latBr;
    @QueryParam("lngBr")
    double lngBr;
    @Context
    private OTPServer otpServer;
    /**
     * @deprecated The support for multiple routers are removed from OTP2. See
     * https://github.com/opentripplanner/OpenTripPlanner/issues/2760
     */
    @Deprecated
    @PathParam("ignoreRouterId")
    private String ignoreRouterId;

    @GET
    @Path("/tile/{layer}/{z}/{x}/{y}.{ext}")
    @Produces("image/*")
    public Response tileGet(
            @PathParam("x") int x, @PathParam("y") int y, @PathParam("z") int z,
            @PathParam("layer") String layer, @PathParam("ext") String ext
    ) throws Exception {

        // Re-use analyst
        Envelope2D env = WebMercatorTile.tile2Envelope(x, y, z);
        MapTile mapTile = new MapTile(env, 256, 256);

        Router router = otpServer.getRouter();
        BufferedImage image = router.tileRendererManager.renderTile(mapTile, layer);

        MIMEImageFormat format = new MIMEImageFormat("image/" + ext);
        ByteArrayOutputStream baos =
                new ByteArrayOutputStream(image.getWidth() * image.getHeight() / 4);
        ImageIO.write(image, format.type, baos);
        CacheControl cc = new CacheControl();
        cc.setMaxAge(3600);
        cc.setNoCache(false);
        return Response.ok(baos.toByteArray()).type(format.toString()).cacheControl(cc).build();
    }

    /**
     * Gets all layer names
     * <p>
     * Used in fronted to create layer chooser
     *
     * @return
     */
    @GET
    @Path("layers")
    @Produces(MediaType.APPLICATION_JSON)
    public InspectorLayersList getLayers() {

        Router router = otpServer.getRouter();
        InspectorLayersList layersList =
                new InspectorLayersList(router.tileRendererManager.getRenderers());
        return layersList;
    }

    @GET
    @Path("variables")
    @Produces(MediaType.APPLICATION_JSON)
    public String getVariables() {

        Router router = otpServer.getRouter();
        var variables =
                router.getGraph().getEdgesOfType(GreenStreetEdge.class).get(0).getScores().keySet();
        return new JSONObject(Map.of("variables", new ArrayList<>(variables))).toJSONString();
    }

    @GET
    @Path("/green/props")
    @Produces(MediaType.APPLICATION_JSON)
    public String getGreenVariables() {
        var props = getProps();
        return new JSONObject(Map.of("variables", new ArrayList<>(props))).toJSONString();
    }

    @GET
    @Path("/comp/{prop}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getEdgesPropertiesCompressed(@PathParam("prop") String prop) {
        String collection;

        var props = getProps();

        if (prop.equals("all")) {collection = getGreenEdgesAsFeaturesComp(props);}
        /*else if (prop.equals("others")) {collection = getOtherThanGreenEdges();}*/
        else if (props.contains(prop)) {collection = getGreenEdgesAsFeaturesComp(Set.of(prop));}
        /*else {collection = FeatureCollection.fromFeatures(List.of());}*/
        else {collection = "";}

        return collection;
    }

    @GET
    @Path("/green/{prop}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getEdgesProperties(@PathParam("prop") String prop) {
        FeatureCollection collection;

        var props = getProps();

        if (prop.equals("all")) {collection = getGreenEdgesAsFeatures(props);}
        else if (prop.equals("others")) {collection = getOtherThanGreenEdges();}
        else if (props.contains(prop)) {collection = getGreenEdgesAsFeatures(Set.of(prop));}
        else {collection = FeatureCollection.fromFeatures(List.of());}

        return collection.toJson();
    }

    @GET
    @Path("/json/{layer}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getGeoJson(@PathParam("layer") String feature) {
        var router = otpServer.getRouter();
        var graph = router.getGraph();
        FeatureCollection collection;

        if (feature.equals("green")) {collection = getGreenEdgesAsFeatures();}
        else if (feature.equals("others")) {collection = getOtherThanGreenEdges();}
        else {collection = getEdgesWithScore(feature);}

        var r = collection.toJson();
        return collection.toJson();
    }

    private FeatureCollection getGreenEdgesAsFeatures(Collection<String> params) {
        var router = otpServer.getRouter();
        var graph = router.getGraph();
        var features = getEdgesForEnvelope(latTl, latBr, lngTl, lngBr)
                .stream()
                .filter(e -> e instanceof GreenStreetEdge)
                .map(e -> (GreenStreetEdge) e)
                .map(e -> toFeature(e, params))
                .collect(Collectors.toList());

        return FeatureCollection.fromFeatures(features);
    }

    private FeatureCollection getOtherThanGreenEdges() {
        var router = otpServer.getRouter();
        var graph = router.getGraph();

        var features = getEdgesForEnvelope(latTl, latBr, lngTl, lngBr)
                .stream()
                .filter(e -> !(e instanceof GreenStreetEdge) && e instanceof StreetEdge)
                .map(e -> toFeature(e))
                .collect(Collectors.toList());

        return FeatureCollection.fromFeatures(features);
    }

    private Feature toFeature(GreenStreetEdge edge, Collection<String> params) {
        var feature = this.toFeature(edge);
        feature.addNumberProperty("osm_id", edge.wayId);
        feature.addNumberProperty("score", edge.getGreenyness());
        params.forEach(p -> feature.addNumberProperty(p, edge.getScores().get(p)));

        return feature;
    }

    private Feature toFeature(Edge edge) {
        var points = Arrays.stream(edge.getGeometry().getCoordinates())
                .map(p -> Point.fromLngLat(p.x, p.y))
                .collect(Collectors.toList());

        return Feature.fromGeometry(LineString.fromLngLats(points));
    }

    private String getGreenEdgesAsFeaturesComp(Collection<String> params) {
        var router = otpServer.getRouter();
        var graph = router.getGraph();

        JSONObject o = new JSONObject();
        o.put("t", "fc");

        var features = getEdgesForEnvelope(latTl, latBr, lngTl, lngBr)
                .stream()
                .filter(e -> e instanceof GreenStreetEdge)
                .map(e -> (GreenStreetEdge) e)
                .map(e -> toFeatureComp(e, params))
                .collect(Collectors.toList());

        o.put("fs", features);
        return o.toJSONString();
    }

    private JSONObject toFeatureComp(GreenStreetEdge edge, Collection<String> params) {
        var feature = this.toFeatureComp(edge);
        feature.put("p", Map.of("s", edge.getGreenyness()));

        return feature;
    }

    private JSONObject toFeatureComp(Edge edge) {
        var points = Arrays.stream(edge.getGeometry().getCoordinates())
                .map(p -> Point.fromLngLat(p.x, p.y))
                .collect(Collectors.toList());

        JSONObject o = new JSONObject();
        o.put("t", "f");
        o.put("g", (LineString.fromLngLats(points)).toPolyline(5));

        return o;
    }

    private FeatureCollection getEdgesWithScore(String feature) {
        var router = otpServer.getRouter();
        var graph = router.getGraph();

        /*JSONObject o = new JSONObject();

        o.put("type", "FeatureCollection");*/

        var features = graph
                .getStreetIndex()
                .getEdgesForEnvelope(new Envelope(latTl, latBr, lngTl, lngBr))
                .stream()
                .filter(e -> e instanceof GreenStreetEdge)
                .map(e -> toFeature(
                        e.getGeometry(),
                        ((GreenStreetEdge) e).getScores().getOrDefault(feature, 0d)
                ))
                .collect(Collectors.toList());

        //o.put("features", features);
        return FeatureCollection.fromFeatures(features);
        //return o.toJSONString();
    }

    private FeatureCollection getGreenEdgesAsFeatures() {
        var router = otpServer.getRouter();
        var graph = router.getGraph();

        /*JSONObject o = new JSONObject();

        o.put("type", "FeatureCollection");*/

        var features = graph
                .getStreetIndex()
                .getEdgesForEnvelope(new Envelope(latTl, latBr, lngTl, lngBr))
                .stream()
                .filter(e -> e instanceof GreenStreetEdge)
                .map(e -> toFeature(e.getGeometry(), ((GreenStreetEdge) e).getGreenyness()))
                .collect(Collectors.toList());

        //o.put("features", features);
        return FeatureCollection.fromFeatures(features);
        //return o.toJSONString();
    }

    private Collection<Edge> getEdgesForEnvelope(
            double latTl,
            double latBr,
            double lngTl,
            double lngBr
    ) {
        var graph = otpServer.getRouter().getGraph();
        return graph
                .getStreetIndex()
                .getEdgesForEnvelope(new Envelope(latTl, latBr, lngTl, lngBr));
    }

    // Da ripensare
    private Collection<String> getProps() {
        var router = this.otpServer.getRouter();
        var props = router.getGraph().getEdgesOfType(GreenStreetEdge.class)
                .stream()
                .map(e -> e.getScores().keySet())
                .filter(set -> set.size() > 0)
                .findFirst()
                .orElseGet(HashSet::new);

        var feat = router.getGraph().getEdgesOfType(GreenStreetEdge.class)
                .stream()
                .map(e -> e.getFeatures().keySet())
                .filter(set -> set.size() > 0)
                .findFirst()
                .orElseGet(HashSet::new);

        var l = new ArrayList<>(props);
        l.addAll(feat);
        /*props.add("score");*/
        return l;
    }

    @GET
    @Path("/all/{prop}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAllEdgesProperties(@PathParam("prop") String prop) {
        FeatureCollection collection;

        var props = getProps();

        if (prop.equals("all")) {collection = getAllGreenEdgesAsFeatures(props);}
        else if (prop.equals("others")) {collection = getOtherThanGreenEdges();}
        else if (props.contains(prop)) {collection = getAllGreenEdgesAsFeatures(Set.of(prop));}
        else {collection = FeatureCollection.fromFeatures(List.of());}

        return collection.toJson();
    }

    private FeatureCollection getAllGreenEdgesAsFeatures(Collection<String> params) {
        var router = otpServer.getRouter();
        var graph = router.getGraph();

        var features = graph.getEdgesOfType(GreenStreetEdge.class)
        .stream()
        .map(e -> toFeature(e, params))
        .collect(Collectors.toList());

        return FeatureCollection.fromFeatures(features);
    }


    @GET
    @Path("/all/lmited")
    @Produces(MediaType.APPLICATION_JSON)
    public Boolean getGreenEdgesAsLDGeojson(Collection<String> params) {
        var router = otpServer.getRouter();
        var graph = router.getGraph();

        var props = getProps();
        var features = graph.getEdgesOfType(GreenStreetEdge.class)
                .stream()
                .map(e -> toFeature(e, props).toJson())
                .collect(Collectors.toList());

        try (PrintWriter pw = new PrintWriter("ldGeojson.json")) {
            features.forEach(f -> pw.println(f));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private Feature toFeature(org.locationtech.jts.geom.LineString ls, double score) {
        var feature = this.toFeature(ls);
        feature.addNumberProperty("score", score);
        //feature.put("properties", Map.of("score", score));

        //return Feature.fromJson(feature.toJson());
        return feature/*.toJSONString()*/;
    }

    private Feature toFeature(org.locationtech.jts.geom.LineString ls) {
        var points = Arrays.stream(ls.getCoordinates())
                .map(p -> Point.fromLngLat(p.x, p.y))
                .collect(Collectors.toList());

/*        JSONObject o = new JSONObject();
        o.put("type", "Feature");
        o.put("geometry", (LineString.fromLngLats(points)).toPolyline(5));*/

        return Feature.fromGeometry(LineString.fromLngLats(points));
        //return o;
    }

    @POST
    @Path("/ld")
    @Produces(MediaType.APPLICATION_JSON)
    public void createTiles() {
        var c = new GreenRoutingConfig(null, null,0,Set.of(),Set.of(),"3","data/LDGeojson_API.json", "l.txt");
        var g = new GreenRouting<>(c);
        g.writeFiles(otpServer.getRouter().getGraph());
    }

}
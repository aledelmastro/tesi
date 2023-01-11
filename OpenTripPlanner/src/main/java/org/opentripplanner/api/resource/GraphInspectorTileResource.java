package org.opentripplanner.api.resource;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.ws.rs.GET;
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
import org.opentripplanner.ext.greenrouting.edgetype.GreenStreetEdge;
import org.opentripplanner.inspector.TileRenderer;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.standalone.server.OTPServer;
import org.opentripplanner.standalone.server.Router;
import org.opentripplanner.util.PolylineEncoder;

/**
 * Slippy map tile API for rendering various graph information for inspection/debugging purpose
 * (bike safety factor, connectivity...).
 * 
 * One can easily add a new layer by adding the following kind of code to a leaflet map:
 * 
 * <pre>
 *   var bikesafety = new L.TileLayer(
 *      'http://localhost:8080/otp/routers/default/inspector/tile/bike-safety/{z}/{x}/{y}.png',
 *      { maxZoom : 22 });
 *   var map = L.map(...);
 *   L.control.layers(null, { "Bike safety": bikesafety }).addTo(map);
 * </pre>
 * 
 * Tile rendering goes through TileRendererManager which select the appropriate renderer for the
 * given layer.
 * 
 * @see org.opentripplanner.inspector.TileRendererManager
 * @see TileRenderer
 * 
 * @author laurent
 * 
 */
@Path("/routers/{ignoreRouterId}/inspector")
public class GraphInspectorTileResource {

    @Context
    private OTPServer otpServer;

    /**
     * @deprecated The support for multiple routers are removed from OTP2.
     * See https://github.com/opentripplanner/OpenTripPlanner/issues/2760
     */
    @Deprecated @PathParam("ignoreRouterId")
    private String ignoreRouterId;

    @GET @Path("/tile/{layer}/{z}/{x}/{y}.{ext}")
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream(image.getWidth() * image.getHeight() / 4);
        ImageIO.write(image, format.type, baos);
        CacheControl cc = new CacheControl();
        cc.setMaxAge(3600);
        cc.setNoCache(false);
        return Response.ok(baos.toByteArray()).type(format.toString()).cacheControl(cc).build();
    }

    /**
     * Gets all layer names
     * 
     * Used in fronted to create layer chooser
     * @return 
     */
    @GET @Path("layers")
    @Produces(MediaType.APPLICATION_JSON)
    public InspectorLayersList getLayers() {

        Router router = otpServer.getRouter();
        InspectorLayersList layersList = new InspectorLayersList(router.tileRendererManager.getRenderers());
        return layersList;
    }

    @GET @Path("variables")
    @Produces(MediaType.APPLICATION_JSON)
    public String getVariables() {

        Router router = otpServer.getRouter();
        var variables = router.graph.getEdgesOfType(GreenStreetEdge.class).get(0).getVariables().keySet();
        return new JSONObject(Map.of("variables", new ArrayList<>(variables))).toJSONString();
    }

    @QueryParam("latTl")
    double latTl;

    @QueryParam("lngTl")
    double lngTl;

    @QueryParam("latBr")
    double latBr;

    @QueryParam("lngBr")
    double lngBr;

    @GET @Path("/json/{layer}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getGeoJson(@PathParam("layer") String feature) {
        var router = otpServer.getRouter();
        var graph = router.graph;
        FeatureCollection collection;

        if (feature.equals("green"))
            collection = getGreenEdgesAsFeatures();
        else if (feature.equals("others"))
            collection = getOtherThanGreenEdges();
        else
            collection = getEdgesWithScore(feature);

        var r = collection.toJson();
        return collection.toJson();
    }

    private FeatureCollection getEdgesWithScore(String feature) {
        var router = otpServer.getRouter();
        var graph = router.graph;

        /*JSONObject o = new JSONObject();

        o.put("type", "FeatureCollection");*/

        var features = graph
                .getStreetIndex()
                .getEdgesForEnvelope(new Envelope(latTl, latBr, lngTl, lngBr))
                .stream()
                .filter(e -> e instanceof GreenStreetEdge)
                .map(e -> toFeature(e.getGeometry(), ((GreenStreetEdge) e).getVariables().getOrDefault(feature, 0d)))
                .collect(Collectors.toList());

        //o.put("features", features);
        return FeatureCollection.fromFeatures(features);
        //return o.toJSONString();
    }

    private FeatureCollection getGreenEdgesAsFeatures() {
        var router = otpServer.getRouter();
        var graph = router.graph;

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

    private FeatureCollection getOtherThanGreenEdges() {
        var router = otpServer.getRouter();
        var graph = router.graph;

        var features = graph
                .getStreetIndex()
                .getEdgesForEnvelope(new Envelope(latTl, latBr, lngTl, lngBr))
                .stream()
                .filter(e -> !(e instanceof GreenStreetEdge) && e instanceof StreetEdge)
                .map(e -> toFeature(e.getGeometry()))
                .collect(Collectors.toList());

        return FeatureCollection.fromFeatures(features);
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
}
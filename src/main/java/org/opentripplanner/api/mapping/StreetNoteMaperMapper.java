package org.opentripplanner.api.mapping;

import org.opentripplanner.api.model.ApiAlert;
import org.opentripplanner.model.StreetNote;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class StreetNoteMaperMapper {
    private final Locale locale;

    public StreetNoteMaperMapper(Locale locale) {
        this.locale = locale;
    }

    public List<ApiAlert> mapToApi(Set<StreetNote> newAlerts) {
        // Using {@code null} and not an empty set will minimize the JSON removing the
        // {@code alerts} from the result.
        if (newAlerts == null || newAlerts.isEmpty()) {
            return null;
        }

        return newAlerts.stream().map(this::mapToApi).collect(Collectors.toList());
    }

    ApiAlert mapToApi(StreetNote domain) {
        ApiAlert api = new ApiAlert();
        if (domain.note != null) {
            api.alertHeaderText = domain.note.toString(locale);
        }

        if (domain.descriptionText != null) {
            api.alertDescriptionText = domain.descriptionText.toString(locale);
        }

        if (domain.url != null) {
            api.alertUrl = domain.url;
        }

        api.effectiveStartDate = domain.effectiveStartDate;
        api.effectiveEndDate = domain.effectiveEndDate;

        return api;
    }
}

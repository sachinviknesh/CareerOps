package com.careerops.app.dto.match;

import java.util.List;

public record JobImportResponse(
        int totalFetched,
        int importedCount,
        int duplicateCount,
        int matchedCount,
        List<String> sourcesUsed,
        String message
) {}

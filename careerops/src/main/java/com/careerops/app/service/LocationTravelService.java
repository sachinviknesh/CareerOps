package com.careerops.app.service;

import com.careerops.app.entity.Candidate;
import com.careerops.app.entity.Opportunity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class LocationTravelService {

    public record Coordinates(double lat, double lon) {}

    public static class TravelIntel {
        private String mode; // Online, Offline, Hybrid
        private boolean travelRequired;
        private Double distanceKm;
        private String distanceLabel;
        private String estimatedTravelTime;
        private String transportOptions;
        private String approximateCost;
        private boolean accommodationRequired;
        private int locationFitScore;
        private String travelBadge;

        public TravelIntel() {}

        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
        public boolean isTravelRequired() { return travelRequired; }
        public void setTravelRequired(boolean travelRequired) { this.travelRequired = travelRequired; }
        public Double getDistanceKm() { return distanceKm; }
        public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
        public String getDistanceLabel() { return distanceLabel; }
        public void setDistanceLabel(String distanceLabel) { this.distanceLabel = distanceLabel; }
        public String getEstimatedTravelTime() { return estimatedTravelTime; }
        public void setEstimatedTravelTime(String estimatedTravelTime) { this.estimatedTravelTime = estimatedTravelTime; }
        public String getTransportOptions() { return transportOptions; }
        public void setTransportOptions(String transportOptions) { this.transportOptions = transportOptions; }
        public String getApproximateCost() { return approximateCost; }
        public void setApproximateCost(String approximateCost) { this.approximateCost = approximateCost; }
        public boolean isAccommodationRequired() { return accommodationRequired; }
        public void setAccommodationRequired(boolean accommodationRequired) { this.accommodationRequired = accommodationRequired; }
        public int getLocationFitScore() { return locationFitScore; }
        public void setLocationFitScore(int locationFitScore) { this.locationFitScore = locationFitScore; }
        public String getTravelBadge() { return travelBadge; }
        public void setTravelBadge(String travelBadge) { this.travelBadge = travelBadge; }
    }

    private static final Map<String, Coordinates> CITY_COORDINATES = new HashMap<>();

    static {
        CITY_COORDINATES.put("chennai", new Coordinates(13.0827, 80.2707));
        CITY_COORDINATES.put("bangalore", new Coordinates(12.9716, 77.5946));
        CITY_COORDINATES.put("bengaluru", new Coordinates(12.9716, 77.5946));
        CITY_COORDINATES.put("hyderabad", new Coordinates(17.3850, 78.4867));
        CITY_COORDINATES.put("mumbai", new Coordinates(19.0760, 72.8777));
        CITY_COORDINATES.put("pune", new Coordinates(18.5204, 73.8567));
        CITY_COORDINATES.put("delhi", new Coordinates(28.6139, 77.2090));
        CITY_COORDINATES.put("new delhi", new Coordinates(28.6139, 77.2090));
        CITY_COORDINATES.put("noida", new Coordinates(28.5355, 77.3910));
        CITY_COORDINATES.put("gurgaon", new Coordinates(28.4595, 77.0266));
        CITY_COORDINATES.put("gurugram", new Coordinates(28.4595, 77.0266));
        CITY_COORDINATES.put("kolkata", new Coordinates(22.5726, 88.3639));
        CITY_COORDINATES.put("ahmedabad", new Coordinates(23.0225, 72.5714));
        CITY_COORDINATES.put("jaipur", new Coordinates(26.9124, 75.7873));
        CITY_COORDINATES.put("coimbatore", new Coordinates(11.0168, 76.9558));
        CITY_COORDINATES.put("kochi", new Coordinates(9.9312, 76.2673));
        CITY_COORDINATES.put("cochin", new Coordinates(9.9312, 76.2673));
        CITY_COORDINATES.put("thiruvananthapuram", new Coordinates(8.5241, 76.9366));
        CITY_COORDINATES.put("trivandrum", new Coordinates(8.5241, 76.9366));
        CITY_COORDINATES.put("chandigarh", new Coordinates(30.7333, 76.7794));
        CITY_COORDINATES.put("indore", new Coordinates(22.7196, 75.8577));
        CITY_COORDINATES.put("bhopal", new Coordinates(23.2599, 77.4126));
        CITY_COORDINATES.put("lucknow", new Coordinates(26.8467, 80.9462));
        CITY_COORDINATES.put("kanpur", new Coordinates(26.4499, 80.3319));
        CITY_COORDINATES.put("patna", new Coordinates(25.5941, 85.1376));
        CITY_COORDINATES.put("bhubaneswar", new Coordinates(20.2961, 85.8245));
        CITY_COORDINATES.put("visakhapatnam", new Coordinates(17.6868, 83.2185));
        CITY_COORDINATES.put("vizag", new Coordinates(17.6868, 83.2185));
        CITY_COORDINATES.put("nagpur", new Coordinates(21.1458, 79.0882));
        CITY_COORDINATES.put("varanasi", new Coordinates(25.3176, 82.9739));
        CITY_COORDINATES.put("madurai", new Coordinates(9.9252, 78.1198));
        CITY_COORDINATES.put("trichy", new Coordinates(10.7905, 78.7047));
        CITY_COORDINATES.put("tiruchirappalli", new Coordinates(10.7905, 78.7047));
    }

    public TravelIntel analyzeTravel(Opportunity opportunity, Candidate candidate) {
        TravelIntel intel = new TravelIntel();

        String workMode = opportunity.getWorkMode() != null ? opportunity.getWorkMode().trim().toLowerCase(Locale.ROOT) : "offline";
        boolean isOnline = workMode.contains("online") || workMode.contains("remote") ||
                (opportunity.getLocation() != null && opportunity.getLocation().toLowerCase(Locale.ROOT).contains("online"));
        boolean isHybrid = workMode.contains("hybrid");

        if (isOnline) {
            intel.setMode("Online");
            intel.setTravelRequired(false);
            intel.setTravelBadge("🟢 Online — No travel required");
            intel.setDistanceKm(0.0);
            intel.setDistanceLabel("No travel required");
            intel.setEstimatedTravelTime("Immediate (Online / Virtual Event)");
            intel.setTransportOptions("High-speed Internet & Browser / Zoom");
            intel.setApproximateCost("Free (No Travel Needed)");
            intel.setAccommodationRequired(false);
            intel.setLocationFitScore(100);
            return intel;
        }

        if (isHybrid) {
            intel.setMode("Hybrid");
            intel.setTravelRequired(true);
            intel.setTravelBadge("🟡 Hybrid — Travel may be required");
        } else {
            intel.setMode("Offline");
            intel.setTravelRequired(true);
            intel.setTravelBadge("🔴 Offline — Travel required");
        }

        // Multi-day check for accommodation requirement
        if (opportunity.getEventStartDate() != null && opportunity.getEventEndDate() != null) {
            if (opportunity.getEventEndDate().isAfter(opportunity.getEventStartDate())) {
                intel.setAccommodationRequired(true);
            }
        }

        String candidateCity = candidate != null ? candidate.getCurrentCity() : null;
        if (candidateCity == null && candidate != null && candidate.getLocation() != null) {
            candidateCity = extractCity(candidate.getLocation());
        }

        String oppCity = opportunity.getCity();
        if (oppCity == null && opportunity.getLocation() != null) {
            oppCity = extractCity(opportunity.getLocation());
        }

        // Calculate distance
        Double distance = calculateDistance(candidateCity, oppCity);
        intel.setDistanceKm(distance);

        int maxDistanceAllowed = (candidate != null && candidate.getMaxTravelDistanceKm() != null)
                ? candidate.getMaxTravelDistanceKm()
                : 300;
        boolean willingToTravel = candidate == null || candidate.isWillingToTravel();

        if (distance == null) {
            intel.setDistanceLabel("Location: " + (opportunity.getLocation() != null ? opportunity.getLocation() : "India"));
            intel.setEstimatedTravelTime("Travel estimate unavailable.");
            intel.setTransportOptions("Direct flights / Railways / Roadways");
            intel.setApproximateCost("Travel estimate unavailable.");
            intel.setLocationFitScore(willingToTravel ? 70 : 40);
        } else if (distance < 30) {
            intel.setDistanceLabel("Approximately " + Math.round(distance) + " km away (Same City / Metro Area)");
            intel.setEstimatedTravelTime("30 - 60 mins by Metro / Local Cab");
            intel.setTransportOptions("City Metro, Public Bus, Rideshare Cabs");
            intel.setApproximateCost("₹50 - ₹200 (Metro / Local)");
            intel.setLocationFitScore(100);
            intel.setAccommodationRequired(false);
        } else {
            intel.setDistanceLabel("Approximately " + Math.round(distance) + " km away");
            computeTravelOptions(intel, distance);

            // Compute location fit score
            if (!willingToTravel) {
                intel.setLocationFitScore(Math.max(15, 40 - (int)(distance / 20)));
            } else if (distance <= maxDistanceAllowed) {
                // High location fit (75-95%)
                int fit = 95 - (int)((distance / maxDistanceAllowed) * 20);
                intel.setLocationFitScore(Math.max(70, fit));
            } else {
                // Exceeds max travel distance
                int penalty = (int)(((distance - maxDistanceAllowed) / 100) * 10);
                intel.setLocationFitScore(Math.max(20, 70 - penalty));
            }
        }

        return intel;
    }

    private void computeTravelOptions(TravelIntel intel, double distance) {
        if (distance <= 150) {
            intel.setEstimatedTravelTime("2 - 3 hours by Intercity Express Train / Bus");
            intel.setTransportOptions("Express Intercity Trains, KSRTC / SETC Deluxe Buses, Self-drive");
            intel.setApproximateCost("₹250 - ₹600 (Train / Bus)");
        } else if (distance <= 400) {
            intel.setEstimatedTravelTime("5 - 7 hours by Vande Bharat / Superfast Express Train");
            intel.setTransportOptions("Indian Railways (Vande Bharat / Shatabdi), AC Sleeper Bus");
            intel.setApproximateCost("₹600 - ₹1,500 (Train / Bus)");
        } else if (distance <= 900) {
            intel.setEstimatedTravelTime("1.5 hours by Direct Flight or 12 - 14 hours by Overnight Train");
            intel.setTransportOptions("Domestic Flight (IndiGo/Air India), Overnight Express Train");
            intel.setApproximateCost("₹1,200 - ₹4,500 (Train 3AC / Flight)");
            intel.setAccommodationRequired(true);
        } else {
            intel.setEstimatedTravelTime("2.5 - 3 hours by Flight or 24+ hours by Superfast Train");
            intel.setTransportOptions("Domestic Direct Flight (Connecting Major Airports), Rajdhani Express");
            intel.setApproximateCost("₹3,500 - ₹7,500 (Economy Flight)");
            intel.setAccommodationRequired(true);
        }
    }

    public Double calculateDistance(String city1, String city2) {
        if (city1 == null || city2 == null) return null;
        Coordinates c1 = findCoordinates(city1);
        Coordinates c2 = findCoordinates(city2);
        if (c1 == null || c2 == null) return null;

        return haversineKm(c1.lat, c1.lon, c2.lat, c2.lon);
    }

    public Coordinates findCoordinates(String cityName) {
        if (cityName == null) return null;
        String clean = cityName.trim().toLowerCase(Locale.ROOT);
        for (Map.Entry<String, Coordinates> entry : CITY_COORDINATES.entrySet()) {
            if (clean.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(R * c * 10.0) / 10.0;
    }

    private String extractCity(String location) {
        if (location == null) return null;
        String[] parts = location.split(",");
        return parts[0].trim();
    }
}

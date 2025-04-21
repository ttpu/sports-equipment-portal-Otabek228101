import java.util.*;
import java.util.stream.Collectors;

public class Sports {
    private Set<String> activities = new TreeSet<>();
    private Map<String, Set<String>> categoryToActivities = new HashMap<>();
    private Map<String, Set<String>> activityToCategories = new HashMap<>();
    private Map<String, Product> products = new TreeMap<>();
    private Map<String, List<Rating>> productRatings = new HashMap<>();

    private class Product {
        String name;
        String activity;
        String category;

        public Product(String name, String activity, String category) {
            this.name = name;
            this.activity = activity;
            this.category = category;
        }
    }

    private class Rating {
        String productName;
        String userName;
        int stars;
        String comment;

        public Rating(String productName, String userName, int stars, String comment) {
            this.productName = productName;
            this.userName = userName;
            this.stars = stars;
            this.comment = comment;
        }

        @Override
        public String toString() {
            return stars + " : " + comment;
        }
    }

    public void defineActivities(String... activities) throws SportsException {
        if (activities.length == 0) {
            throw new SportsException("No activities provided");
        }
        
        for (String activity : activities) {
            this.activities.add(activity);
            if (!activityToCategories.containsKey(activity)) {
                activityToCategories.put(activity, new TreeSet<>());
            }
        }
    }

    public List<String> getActivities() {
        return new ArrayList<>(activities);
    }

    public void addCategory(String name, String... linkedActivities) throws SportsException {
        for (String activity : linkedActivities) {
            if (!activities.contains(activity)) {
                throw new SportsException("Activity " + activity + " does not exist");
            }
        }

        Set<String> linkedActivitiesSet = new HashSet<>(Arrays.asList(linkedActivities));
        categoryToActivities.put(name, linkedActivitiesSet);

        for (String activity : linkedActivities) {
            activityToCategories.get(activity).add(name);
        }
    }

    public int countCategories() {
        return categoryToActivities.size();
    }

    public List<String> getCategoriesForActivity(String activity) {
        if (!activityToCategories.containsKey(activity)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(activityToCategories.get(activity));
    }

    public void addProduct(String name, String activityName, String categoryName) throws SportsException {
        if (products.containsKey(name)) {
            throw new SportsException("Product " + name + " already exists");
        }
        
        Product product = new Product(name, activityName, categoryName);
        products.put(name, product);
        
        productRatings.putIfAbsent(name, new ArrayList<>());
    }

    public List<String> getProductsForCategory(String categoryName) {
        return products.values().stream()
                .filter(p -> p.category.equals(categoryName))
                .map(p -> p.name)
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getProductsForActivity(String activityName) {
        return products.values().stream()
                .filter(p -> p.activity.equals(activityName))
                .map(p -> p.name)
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getProducts(String activityName, String... categoryNames) {
        Set<String> categories = new HashSet<>(Arrays.asList(categoryNames));
        
        return products.values().stream()
                .filter(p -> p.activity.equals(activityName) && categories.contains(p.category))
                .map(p -> p.name)
                .sorted()
                .collect(Collectors.toList());
    }

    public void addRating(String productName, String userName, int numStars, String comment) throws SportsException {
        if (numStars < 0 || numStars > 5) {
            throw new SportsException("Star rating must be between 0 and 5");
        }
        
        Rating rating = new Rating(productName, userName, numStars, comment);
        
        productRatings.putIfAbsent(productName, new ArrayList<>());
        productRatings.get(productName).add(rating);
    }

    public List<String> getRatingsForProduct(String productName) {
        if (!productRatings.containsKey(productName)) {
            return new ArrayList<>();
        }
        
        return productRatings.get(productName).stream()
                .sorted((r1, r2) -> Integer.compare(r2.stars, r1.stars))
                .map(Rating::toString)
                .collect(Collectors.toList());
    }

    public double getStarsOfProduct(String productName) {
        if (!productRatings.containsKey(productName) || productRatings.get(productName).isEmpty()) {
            return 0.0;
        }
        
        List<Rating> ratings = productRatings.get(productName);
        double sum = ratings.stream().mapToInt(r -> r.stars).sum();
        return sum / ratings.size();
    }

    public double averageStars() {
        long totalRatings = productRatings.values().stream()
                .mapToLong(List::size)
                .sum();
                
        if (totalRatings == 0) {
            return 0.0;
        }
        
        double totalStars = productRatings.values().stream()
                .flatMap(List::stream)
                .mapToInt(r -> r.stars)
                .sum();
                
        return totalStars / totalRatings;
    }

    public SortedMap<String, Double> starsPerActivity() {
        SortedMap<String, Double> result = new TreeMap<>();
        
        Map<String, List<String>> productsByActivity = new HashMap<>();
        for (Product product : products.values()) {
            productsByActivity.putIfAbsent(product.activity, new ArrayList<>());
            productsByActivity.get(product.activity).add(product.name);
        }
        
        for (Map.Entry<String, List<String>> entry : productsByActivity.entrySet()) {
            String activity = entry.getKey();
            List<String> activityProducts = entry.getValue();
            
            int totalStars = 0;
            int ratingCount = 0;
            
            for (String productName : activityProducts) {
                if (productRatings.containsKey(productName)) {
                    List<Rating> ratings = productRatings.get(productName);
                    if (!ratings.isEmpty()) {
                        totalStars += ratings.stream().mapToInt(r -> r.stars).sum();
                        ratingCount += ratings.size();
                    }
                }
            }
            
            if (ratingCount > 0) {
                result.put(activity, (double) totalStars / ratingCount);
            }
        }
        
        return result;
    }

    public SortedMap<Double, List<String>> getProductsPerStars() {
        SortedMap<Double, List<String>> result = new TreeMap<>(Comparator.reverseOrder());
        
        Map<String, Double> productAverageStars = new HashMap<>();
        for (String productName : products.keySet()) {
            double avgStars = getStarsOfProduct(productName);
            if (avgStars > 0) {
                productAverageStars.put(productName, avgStars);
            }
        }
        
        for (Map.Entry<String, Double> entry : productAverageStars.entrySet()) {
            String productName = entry.getKey();
            Double stars = entry.getValue();
            
            result.putIfAbsent(stars, new ArrayList<>());
            result.get(stars).add(productName);
        }
        
        for (List<String> productList : result.values()) {
            Collections.sort(productList);
        }
        
        return result;
    }
}
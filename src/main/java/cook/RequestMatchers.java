package cook;

import java.util.function.Predicate;

public abstract class RequestMatchers {

    public static Predicate<Request> uriEqualsCaseSensitive(String uri) {
        return (Request r) -> r.uriEqualsMatchCase(uri);
    }
}

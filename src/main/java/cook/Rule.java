package cook;

import java.util.function.Predicate;

public interface Rule extends Predicate<Request>, RequestHandler {

    String handle(Request request);
}

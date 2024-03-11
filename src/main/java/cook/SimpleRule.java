package cook;

import java.util.function.Predicate;

public class SimpleRule implements Rule {

    private final Predicate<Request> predicate;
    private final RequestHandler handler;

    public SimpleRule(Predicate<Request> predicate, RequestHandler handler) {
        this.predicate = predicate;
        this.handler = handler;
    }

    @Override
    public String handle(Request request) {
        return handler.handle(request);
    }

    @Override
    public boolean test(Request t) {
        return predicate.test(t);
    }

}

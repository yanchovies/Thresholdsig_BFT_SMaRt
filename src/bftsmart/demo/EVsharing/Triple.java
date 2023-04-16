package bftsmart.demo.EVsharing;

import java.io.Serial;
import java.io.Serializable;

/**
 * This helper class implements a triple of objects.
 *
 */

public class Triple <A, B, C> implements Serializable {
    @Serial
    private static final long serialVersionUID = 4L;
    private A first;
    private B second;
    private C third;

    public Triple(A first, B second, C third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    public C getThird() {
        return third;
    }

}

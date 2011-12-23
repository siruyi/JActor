package org.agilewiki.jactor.apc;

/**
 * <p>
 * Iterates over its process method and works within any actor which supports 2-way messages.
 * </p>
 * <p>
 * We can use JAIterator to calculate factorials:
 * </p>
 * <pre>
 * int max = 5;
 * ResponseProcessor printResult = new ResponseProcessor() throws Exception {
 *     public void process(Object rsp) {
 *         System.out.println(rsp);
 *     }
 * };
 *
 * new JAIterator(printResult) {
 *     int i = 0;
 *     int r = 1;
 *
 *     public void process(ResponseProcessor rp) throws Exception {
 *         if (i > max) rp.process(new Integer(r);
 *         else {
 *             i += 1;
 *             r = r * i;
 *             rp.process(null)
 *         }
 *     }
 * };
 * </pre>
 * <p>
 * The JAIterator.process method is called repeatedly until it returns a non-null response,
 * which is then returned by JAIterator. However, the response need not be returned immediately, so the
 * JAIterator.process method can send messages to other actors:
 * </p>
 * <pre>
 * int max = 5;
 * ResponseProcessor printResult = new ResponseProcessor() {
 *     public void process(Object rsp) throws Exception {
 *         System.out.println(rsp);
 *     }
 * };
 *
 * new JAIterator(printResult) {
 *     int i = 0;
 *     int r = 1;
 *
 *     public void process(ResponseProcessor rp) throws Exception {
 *         if (i > max) rp.process(new Integer(r));
 *         else {
 *             i += 1;
 *             Multiply m = new Multiply(r, i);
 *             send(anotherActor, m, new ResponseProcessor() {
 *                 public void process(Object rsp) throws Exception {
 *                     r = ((Integer) rsp).intValue();
 *                 }
 *             });
 *         }
 *     }
 * };
 * </pre>
 */
abstract public class JAIterator {

    /**
     * Set to true when the response has not been received asynchronously.
     */
    private boolean sync = false;

    /**
     * Set to true when the response has not been received synchronously.
     */
    private boolean async = false;

    /**
     * Processes the final, non-null result.
     */
    private final ResponseProcessor responseProcessor;

    /**
     * The internal ResponseProcessor which handles the responses from the call to process.
     */
    private final ResponseProcessor irp = new ResponseProcessor() {
        @Override
        public void process(Object unwrappedResponse) throws Exception {
            if (unwrappedResponse == null) {
                if (!async) {
                    sync = true;
                } else {
                    iterate(); //not recursive
                }
            } else responseProcessor.process(unwrappedResponse);
        }
    };

    /**
     * Create a JAIterator.
     *
     * @param responseProcessor Processes the final, non-null result.
     * @throws Exception Any uncaught exceptions raised by the process method.
     */
    public JAIterator(ResponseProcessor responseProcessor) throws Exception {
        this.responseProcessor = responseProcessor;
        iterate();
    }

    /**
     * Iterates over the process method.
     *
     * @throws Exception Any uncaught exceptions raised by the process method.
     */
    private void iterate() throws Exception {
        sync = true;
        while (sync) {
            sync = false;
            process(irp);
            if (!sync) {
                async = true;
            }
        }
    }

    /**
     * Perform an iteration.
     *
     * @param responseProcessor Processes the response.
     * @throws Exception Any uncaught exceptions raised by the process method.
     */
    abstract protected void process(ResponseProcessor responseProcessor) throws Exception;
}

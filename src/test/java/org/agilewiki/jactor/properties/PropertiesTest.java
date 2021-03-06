package org.agilewiki.jactor.properties;

import junit.framework.TestCase;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.MailboxFactory;

/**
 * Test code.
 */
public class PropertiesTest extends TestCase {
    public void test() {
        MailboxFactory mailboxFactory = JAMailboxFactory.newMailboxFactory(1);
        try {
            JAProperties p1 = new JAProperties();
            p1.initialize(mailboxFactory.createMailbox());
            JAProperties p2 = new JAProperties();
            p2.initialize(p1.getMailbox(), p1);
            (new SetProperty("a", "foo")).call(p1);
            (new SetProperty("b", "bar")).call(p2);
            String a = (new GetProperty<String>("a")).call(p2);
            System.out.println(a);
            String b = (new GetProperty<String>("b")).call(p2);
            System.out.println(b);
            String c = (new GetProperty<String>("c")).call(p2);
            System.out.println(c);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mailboxFactory.close();
        }
    }
}

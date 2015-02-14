package org.jooby.integration.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jooby.Session;
import org.jooby.test.ServerFeature;
import org.junit.Test;

public class ShouldPreventSaveOnUnmodifiedSessionFeature extends ServerFeature {

  private static final CountDownLatch latch = new CountDownLatch(2);

  {

    use(new Session.MemoryStore() {

      @Override
      public void create(final Session session) {
        super.create(session);
        latch.countDown();
      }

      @Override
      public void save(final Session session) {
        super.save(session);
        latch.countDown();
      }
    });

    get("/shouldPreventSaveOnUnmodifiedSession", req -> {
      Session session = req.session();
      session.set("k1", "v1");
      return session.get("k1").get();
    });

  }

  @Test
  public void shouldPreventSaveOnUnmodifiedSession() throws Exception {
    request()
        .get("/shouldPreventSaveOnUnmodifiedSession")
        .expect(200)
        .header("Set-Cookie", setCookie -> assertNotNull(setCookie));

    request()
        .get("/shouldPreventSaveOnUnmodifiedSession")
        .expect(200)
        .header("Set-Cookie", (String) null);

    request()
        .get("/shouldPreventSaveOnUnmodifiedSession")
        .expect(200)
        .header("Set-Cookie", (String) null);

    latch.await(1000, TimeUnit.MILLISECONDS);
    assertEquals(1, latch.getCount());
  }

}

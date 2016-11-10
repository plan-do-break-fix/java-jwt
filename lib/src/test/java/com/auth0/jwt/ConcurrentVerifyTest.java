package com.auth0.jwt;

import com.auth0.jwt.algorithms.Algorithm;
import net.jodah.concurrentunit.Waiter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.security.interfaces.ECKey;
import java.security.interfaces.RSAKey;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import static com.auth0.jwt.PemUtils.readPublicKeyFromFile;

public class ConcurrentVerifyTest {

    private static final long TIMEOUT = 60 * 1000 * 1000; //1 min
    private static final int THREAD_COUNT = 1500;
    private static final int REPEAT_COUNT = 10000;
    private static final String PUBLIC_KEY_FILE = "src/test/resources/rsa-public.pem";
    private static final String PUBLIC_KEY_FILE_256 = "src/test/resources/ec256-key-public.pem";
    private static final String PUBLIC_KEY_FILE_384 = "src/test/resources/ec384-key-public.pem";
    private static final String PUBLIC_KEY_FILE_512 = "src/test/resources/ec512-key-public.pem";

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private static ExecutorService executor;

    @BeforeClass
    public static void beforeAll() throws Exception {
        executor = Executors.newFixedThreadPool(THREAD_COUNT);
    }

    @AfterClass
    public static void afterAll() throws Exception {
        executor.shutdown();
    }

    @SuppressWarnings("Convert2Lambda")
    private void concurrentVerify(final JWTVerifier verifier, final String token) throws TimeoutException {
        final Waiter waiter = new Waiter();
        for (int i = 0; i < REPEAT_COUNT; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    JWT result = null;
                    try {
                        result = verifier.verify(token);
                    } catch (Exception e) {
                        waiter.fail(e);
                    }
                    waiter.assertNotNull(result);
                    waiter.resume();
                }
            });
        }

        waiter.await(TIMEOUT, REPEAT_COUNT);
    }

    @Test
    public void shouldPassHMAC256Verification() throws Exception {
        Algorithm algorithm = Algorithm.HMAC256("secret");
        JWTVerifier verifier = JWTVerifier.init(algorithm).withIssuer("auth0").build();
        String token = "eyJhbGciOiJIUzI1NiIsImN0eSI6IkpXVCJ9.eyJpc3MiOiJhdXRoMCJ9.mZ0m_N1J4PgeqWmi903JuUoDRZDBPB7HwkS4nVyWH1M";

        concurrentVerify(verifier, token);
    }

    @Test
    public void shouldPassHMAC384Verification() throws Exception {
        String token = "eyJhbGciOiJIUzM4NCIsImN0eSI6IkpXVCJ9.eyJpc3MiOiJhdXRoMCJ9.uztpK_wUMYJhrRv8SV-1LU4aPnwl-EM1q-wJnqgyb5DHoDteP6lN_gE1xnZJH5vw";
        Algorithm algorithm = Algorithm.HMAC384("secret");
        JWTVerifier verifier = JWTVerifier.init(algorithm).withIssuer("auth0").build();

        concurrentVerify(verifier, token);
    }

    @Test
    public void shouldPassHMAC512Verification() throws Exception {
        String token = "eyJhbGciOiJIUzUxMiIsImN0eSI6IkpXVCJ9.eyJpc3MiOiJhdXRoMCJ9.VUo2Z9SWDV-XcOc_Hr6Lff3vl7L9e5Vb8ThXpmGDFjHxe3Dr1ZBmUChYF-xVA7cAdX1P_D4ZCUcsv3IefpVaJw";
        Algorithm algorithm = Algorithm.HMAC512("secret");
        JWTVerifier verifier = JWTVerifier.init(algorithm).withIssuer("auth0").build();

        concurrentVerify(verifier, token);
    }

    @Test
    public void shouldPassRSA256Verification() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJhdXRoMCJ9.dxXF3MdsyW-AuvwJpaQtrZ33fAde9xWxpLIg9cO2tMLH2GSRNuLAe61KsJusZhqZB9Iy7DvflcmRz-9OZndm6cj_ThGeJH2LLc90K83UEvvRPo8l85RrQb8PcanxCgIs2RcZOLygERizB3pr5icGkzR7R2y6zgNCjKJ5_NJ6EiZsGN6_nc2PRK_DbyY-Wn0QDxIxKoA5YgQJ9qafe7IN980pXvQv2Z62c3XR8dYuaXBqhthBj-AbaFHEpZapN-V-TmuLNzR2MCB6Xr7BYMuCaqWf_XU8og4XNe8f_8w9Wv5vvgqMM1KhqVpG5VdMJv4o_L4NoCROHhtUQSLRh2M9cA";
        Algorithm algorithm = Algorithm.RSA256((RSAKey) readPublicKeyFromFile(PUBLIC_KEY_FILE, "RSA"));
        JWTVerifier verifier = JWTVerifier.init(algorithm).withIssuer("auth0").build();

        concurrentVerify(verifier, token);
    }

    @Test
    public void shouldPassRSA384Verification() throws Exception {
        String token = "eyJhbGciOiJSUzM4NCIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJhdXRoMCJ9.TZlWjXObwGSQOiu2oMq8kiKz0_BR7bbBddNL6G8eZ_GoR82BXOZDqNrQr7lb_M-78XGBguWLWNIdYhzgxOUL9EoCJlrqVm9s9vo6G8T1sj1op-4TbjXZ61TwIvrJee9BvPLdKUJ9_fp1Js5kl6yXkst40Th8Auc5as4n49MLkipjpEhKDKaENKHpSubs1ripSz8SCQZSofeTM_EWVwSw7cpiM8Fy8jOPvWG8Xz4-e3ODFowvHVsDcONX_4FTMNbeRqDuHq2ZhCJnEfzcSJdrve_5VD5fM1LperBVslTrOxIgClOJ3RmM7-WnaizJrWP3D6Z9OLxPxLhM6-jx6tcxEw";
        Algorithm algorithm = Algorithm.RSA384((RSAKey) readPublicKeyFromFile(PUBLIC_KEY_FILE, "RSA"));
        JWTVerifier verifier = JWTVerifier.init(algorithm).withIssuer("auth0").build();

        concurrentVerify(verifier, token);
    }

    @Test
    public void shouldPassRSA512Verification() throws Exception {
        String token = "eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJhdXRoMCJ9.mvL5LoMyIrWYjk5umEXZTmbyIrkbbcVPUkvdGZbu0qFBxGOf0nXP5PZBvPcOu084lvpwVox5n3VaD4iqzW-PsJyvKFgi5TnwmsbKchAp7JexQEsQOnTSGcfRqeUUiBZqRQdYsho71oAB3T4FnalDdFEpM-fztcZY9XqKyayqZLreTeBjqJm4jfOWH7KfGBHgZExQhe96NLq1UA9eUyQwdOA1Z0SgXe4Ja5PxZ6Fm37KnVDtDlNnY4JAAGFo6y74aGNnp_BKgpaVJCGFu1f1S5xCQ1HSvs8ZSdVWs5NgawW3wRd0kRt_GJ_Y3mIwiF4qUyHWGtsSHu_qjVdCTtbFyow";
        Algorithm algorithm = Algorithm.RSA512((RSAKey) readPublicKeyFromFile(PUBLIC_KEY_FILE, "RSA"));
        JWTVerifier verifier = JWTVerifier.init(algorithm).withIssuer("auth0").build();

        concurrentVerify(verifier, token);
    }

    @Test
    public void shouldPassECDSA256VerificationWithJOSESignature() throws Exception {
        String token = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJhdXRoMCJ9.4iVk3-Y0v4RT4_9IaQlp-8dZ_4fsTzIylgrPTDLrEvTHBTyVS3tgPbr2_IZfLETtiKRqCg0aQ5sh9eIsTTwB1g";
        ECKey key = (ECKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_256, "EC");
        Algorithm algorithm = Algorithm.ECDSA256(key);
        JWTVerifier verifier = JWTVerifier.init(algorithm).withIssuer("auth0").build();

        concurrentVerify(verifier, token);
    }

    @Test
    public void shouldPassECDSA256VerificationWithDERSignature() throws Exception {
        String token = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJhdXRoMCJ9.MEYCIQDiJWTf5jS/hFPj/0hpCWn7x1n/h+xPMjKWCs9MMusS9AIhAMcFPJVLe2A9uvb8hl8sRO2IpGoKDRpDmyH14ixNPAHW";
        ECKey key = (ECKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_256, "EC");
        Algorithm algorithm = Algorithm.ECDSA256(key);
        JWTVerifier verifier = JWTVerifier.init(algorithm).withIssuer("auth0").build();

        concurrentVerify(verifier, token);
    }

    @Test
    public void shouldPassECDSA384VerificationWithJOSESignature() throws Exception {
        String token = "eyJhbGciOiJFUzM4NCJ9.eyJpc3MiOiJhdXRoMCJ9.50UU5VKNdF1wfykY8jQBKpvuHZoe6IZBJm5NvoB8bR-hnRg6ti-CHbmvoRtlLfnHfwITa_8cJMy6TenMC2g63GQHytc8rYoXqbwtS4R0Ko_AXbLFUmfxnGnMC6v4MS_z";
        ECKey key = (ECKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_384, "EC");
        Algorithm algorithm = Algorithm.ECDSA384(key);
        JWTVerifier verifier = JWTVerifier.init(algorithm).withIssuer("auth0").build();

        concurrentVerify(verifier, token);
    }

    @Test
    public void shouldPassECDSA384VerificationWithDERSignature() throws Exception {
        String token = "eyJhbGciOiJFUzM4NCJ9.eyJpc3MiOiJhdXRoMCJ9.MGUCMQDnRRTlUo10XXB/KRjyNAEqm+4dmh7ohkEmbk2+gHxtH6GdGDq2L4Idua+hG2Ut+ccCMH8CE2v/HCTMuk3pzAtoOtxkB8rXPK2KF6m8LUuEdCqPwF2yxVJn8ZxpzAur+DEv8w==";
        ECKey key = (ECKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_384, "EC");
        Algorithm algorithm = Algorithm.ECDSA384(key);
        JWTVerifier verifier = JWTVerifier.init(algorithm).withIssuer("auth0").build();

        concurrentVerify(verifier, token);
    }

    @Test
    public void shouldPassECDSA512VerificationWithJOSESignature() throws Exception {
        String token = "eyJhbGciOiJFUzUxMiJ9.eyJpc3MiOiJhdXRoMCJ9.AeCJPDIsSHhwRSGZCY6rspi8zekOw0K9qYMNridP1Fu9uhrA1QrG-EUxXlE06yvmh2R7Rz0aE7kxBwrnq8L8aOBCAYAsqhzPeUvyp8fXjjgs0Eto5I0mndE2QHlgcMSFASyjHbU8wD2Rq7ZNzGQ5b2MZfpv030WGUajT-aZYWFUJHVg2";
        ECKey key = (ECKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_512, "EC");
        Algorithm algorithm = Algorithm.ECDSA512(key);
        JWTVerifier verifier = JWTVerifier.init(algorithm).withIssuer("auth0").build();

        concurrentVerify(verifier, token);
    }

    @Test
    public void shouldPassECDSA512VerificationWithDERSignature() throws Exception {
        String token = "eyJhbGciOiJFUzUxMiJ9.eyJpc3MiOiJhdXRoMCJ9.MIGIAkIB4Ik8MixIeHBFIZkJjquymLzN6Q7DQr2pgw2uJ0/UW726GsDVCsb4RTFeUTTrK+aHZHtHPRoTuTEHCuerwvxo4EICQgGALKocz3lL8qfH1444LNBLaOSNJp3RNkB5YHDEhQEsox21PMA9kau2TcxkOW9jGX6b9N9FhlGo0/mmWFhVCR1YNg==";
        ECKey key = (ECKey) readPublicKeyFromFile(PUBLIC_KEY_FILE_512, "EC");
        Algorithm algorithm = Algorithm.ECDSA512(key);
        JWTVerifier verifier = JWTVerifier.init(algorithm).withIssuer("auth0").build();

        concurrentVerify(verifier, token);
    }
}
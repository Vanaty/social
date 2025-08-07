package iranga.mg.social;

import org.junit.jupiter.api.Test;
import org.springframework.web.method.ControllerAdviceBean;

import java.net.URL;
import java.security.CodeSource;

public class DependencyTest {

    @Test
    void checkControllerAdviceBeanSource() {
        try {
            Class<ControllerAdviceBean> clazz = ControllerAdviceBean.class;
            CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
            if (codeSource != null) {
                URL location = codeSource.getLocation();
                System.out.println("ControllerAdviceBean is loaded from: " + location);
            } else {
                System.out.println("Could not determine the source for ControllerAdviceBean.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

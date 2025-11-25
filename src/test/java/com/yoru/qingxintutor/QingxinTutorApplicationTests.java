package com.yoru.qingxintutor;

import com.yoru.qingxintutor.utils.EmailUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class QingxinTutorApplicationTests {

    @Autowired
    private EmailUtils emailUtils;

    @Test
    void contextLoads() {
        emailUtils.sendLoginCode("2085509323@qq.com", "123123");
    }


}

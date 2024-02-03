package con.zhuo.im.service.user.service;

import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.enums.UserErrorCode;
import com.zhuo.im.service.user.constants.UserConstants;
import com.zhuo.im.service.user.dao.ImUserDataEntity;
import com.zhuo.im.service.user.dao.mapper.ImUserDataMapper;
import com.zhuo.im.service.user.model.req.ImportUserReq;
import com.zhuo.im.service.user.model.resp.ImportUserResp;
import com.zhuo.im.service.user.service.impl.ImUserServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class ImUserServiceTest {

    @Mock
    private ImUserDataMapper imUserDataMapper;

    @InjectMocks
    private ImUserServiceImpl imUserService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private ImportUserReq generateImportUserReq(int size) {

        int appId = 100;
        ImportUserReq req = new ImportUserReq();
        req.setAppId(appId);
        List<ImUserDataEntity> userDataList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ImUserDataEntity entity = new ImUserDataEntity();
            entity.setAppId(appId);
            entity.setUserId("user" + i);
            userDataList.add(new ImUserDataEntity());
        }
        req.setUserData(userDataList);
        return req;
    }

    @Test
    public void importUser_ExceedsMaxImportSize_ShouldReturnErrorResponse() {

        // Create a sample request
        ImportUserReq req = generateImportUserReq(UserConstants.MAX_IMPORT_SIZE + 1);

        ResponseVO response = imUserService.importUser(req);

        assertNotNull(response);
        assertFalse(response.isOk());
        assertEquals(UserErrorCode.IMPORT_SIZE_EXCEED.getCode(), response.getCode());
    }

    @Test
    public void importUser_ValidData_ShouldReturnSuccessResponse() {
        when(imUserDataMapper.insert(any(ImUserDataEntity.class))).thenReturn(1); // Simulate successful insert

        ImportUserReq req = generateImportUserReq(UserConstants.MAX_IMPORT_SIZE);
        ResponseVO response = imUserService.importUser(req);

        assertNotNull(response);
        assertTrue(response.isOk());
        ImportUserResp resp = (ImportUserResp) response.getData();
        assertNotNull(resp);
        assertEquals(UserConstants.MAX_IMPORT_SIZE, resp.getSuccessId().size()); // Ensure there are success IDs
        assertEquals(0, resp.getErrorId().size()); // Ensure there are no error IDs
    }

}

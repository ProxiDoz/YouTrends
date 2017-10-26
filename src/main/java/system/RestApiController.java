package system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import system.access.UserDAO;

@RestController
public class RestApiController
{
    @Autowired
    private UserDAO userDAO;

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(method = RequestMethod.POST, value = "/login", produces = "application/json")
    public boolean login(@RequestBody Credentials credentials)
    {
        try
        {
            return userDAO.checkCredential(credentials.getLogin(), credentials.getPassword());
        }
        catch (Exception e)
        {
            MyLogger.logErr("login error");
        }

        return false;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(method = RequestMethod.POST, value = "/getUserSettingData", produces = "application/json")
    public UserSettingsData getUserSettingsData(@RequestBody Credentials credentials)
    {
        try
        {
            boolean isAuth = userDAO.checkCredential(credentials.getLogin(), credentials.getPassword());

            if (isAuth)
            {
                return userDAO.getUserSettingsData(credentials.getLogin());
            }
        }
        catch (Exception e)
        {
            MyLogger.logErr("login error");
        }

        return new UserSettingsData();
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(method = RequestMethod.POST, value = "/setUserSettingData", produces = "application/json")
    public UserSettingsData setUserSettingData(@RequestBody UserSettingsData userSettingsData)
    {
        try
        {
            boolean isAuth = userDAO.checkCredential(userSettingsData.getCredentials().getLogin(),
                                                     userSettingsData.getCredentials().getPassword());

            if (isAuth)
            {
                return userDAO.setUserSettingData(userSettingsData);
            }
        }
        catch (Exception e)
        {
            MyLogger.logErr("login error");
        }

        return null;
    }
}

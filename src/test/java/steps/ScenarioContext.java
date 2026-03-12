package steps;

import io.restassured.filter.session.SessionFilter;
import io.restassured.response.Response;

public class ScenarioContext {
    public Response response;
    public String lastProcessedBody;
    public String refreshToken;
    public String accessToken;
    public int createdUserId;
    public String createdTestSlug;
    public String createdTestTitle;
    public SessionFilter sessionFilter = new SessionFilter();
    public String verifiedPasswordCookie;
    public int createdQuestionId;
    public int secondQuestionId;
    public int createdAttemptId;
    public int createdAnswerId;
    public int createdCompanyId;
    public String secondAccessToken;
    public int secondUserId;
    public String inviteToken;
    public int inviteId;
    public String inviteEmail;
    public String inviteUserEmail;
    public int createdFolderId;
    public int secondFolderId;
    public String companyTestSlug;
    public String instructorToken;
    public String studentToken;
}
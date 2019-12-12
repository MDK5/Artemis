package de.tum.in.www1.artemis.web.rest.dto;

public class TutorLeaderboardDTO {

    private long userId;

    private String name;

    private long numberOfAssessments;

    private long numberOfAcceptedComplaints;

    private long numberOfNotAnsweredMoreFeedbackRequests;

    private long numberOfComplaintResponses;

    private long numberOfAnsweredMoreFeedbackRequests;

    private Long points;

    public TutorLeaderboardDTO(long userId, String name, long numberOfAssessments, long numberOfAcceptedComplaints, long numberOfNotAnsweredMoreFeedbackRequests,
            long numberOfComplaintResponses, long numberOfAnsweredMoreFeedbackRequests, Long points) {
        this.userId = userId;
        this.name = name;
        this.numberOfAssessments = numberOfAssessments;
        this.numberOfAcceptedComplaints = numberOfAcceptedComplaints;
        this.numberOfNotAnsweredMoreFeedbackRequests = numberOfNotAnsweredMoreFeedbackRequests;
        this.numberOfComplaintResponses = numberOfComplaintResponses;
        this.numberOfAnsweredMoreFeedbackRequests = numberOfAnsweredMoreFeedbackRequests;
        this.points = points;
    }

    @SuppressWarnings("unused")
    public TutorLeaderboardDTO() {
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getNumberOfAssessments() {
        return numberOfAssessments;
    }

    public void setNumberOfAssessments(long numberOfAssessments) {
        this.numberOfAssessments = numberOfAssessments;
    }

    public long getNumberOfNotAnsweredMoreFeedbackRequests() {
        return numberOfNotAnsweredMoreFeedbackRequests;
    }

    public void setNumberOfNotAnsweredMoreFeedbackRequests(long numberOfNotAnsweredMoreFeedbackRequests) {
        this.numberOfNotAnsweredMoreFeedbackRequests = numberOfNotAnsweredMoreFeedbackRequests;
    }

    public long getNumberOfAcceptedComplaints() {
        return numberOfAcceptedComplaints;
    }

    public void setNumberOfAcceptedComplaints(long numberOfAcceptedComplaints) {
        this.numberOfAcceptedComplaints = numberOfAcceptedComplaints;
    }

    public long getNumberOfComplaintResponses() {
        return numberOfComplaintResponses;
    }

    public void setNumberOfComplaintResponses(long numberOfComplaintResponses) {
        this.numberOfComplaintResponses = numberOfComplaintResponses;
    }

    public long getNumberOfAnsweredMoreFeedbackRequests() {
        return numberOfAnsweredMoreFeedbackRequests;
    }

    public void setNumberOfAnsweredMoreFeedbackRequests(long numberOfAnsweredMoreFeedbackRequests) {
        this.numberOfAnsweredMoreFeedbackRequests = numberOfAnsweredMoreFeedbackRequests;
    }

    public Long getPoints() {
        return points;
    }

    public void setPoints(Long points) {
        this.points = points;
    }
}

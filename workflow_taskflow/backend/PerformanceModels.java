/**
 * PerformanceModels.java
 * Lightweight model classes matching the Performance Management team's
 * models.Goal, models.Appraisal, models.PerformanceCycle, models.Feedback,
 * models.KPI, models.Employee, models.Reward used in their interfaces.
 *
 * These are used by RealPerformanceIntegration to return data from hrms.db
 * in the format the PM team's interfaces expect.
 *
 * Package note: PM team uses 'package models' — here we keep no package
 * for compatibility with the flat backend compilation.
 */

class Goal {
    public String goalId;
    public String employeeId;
    public String goalTitle;
    public String goalDescription;
    public String goalStatus;      // "Not Started", "In Progress", "Completed"
    public String goalStartDate;
    public String goalEndDate;

    public Goal(String goalId, String employeeId, String goalTitle,
                String goalDescription, String goalStatus,
                String goalStartDate, String goalEndDate) {
        this.goalId          = goalId;
        this.employeeId      = employeeId;
        this.goalTitle       = goalTitle;
        this.goalDescription = goalDescription;
        this.goalStatus      = goalStatus;
        this.goalStartDate   = goalStartDate;
        this.goalEndDate     = goalEndDate;
    }
}

class Appraisal {
    public String appraisalId;
    public String employeeId;
    public String reviewerId;
    public float  score;
    public String status;
    public String period;
    public String appraisalDate;

    public Appraisal(String appraisalId, String employeeId, String reviewerId,
                     float score, String status, String period, String appraisalDate) {
        this.appraisalId  = appraisalId;
        this.employeeId   = employeeId;
        this.reviewerId   = reviewerId;
        this.score        = score;
        this.status       = status;
        this.period       = period;
        this.appraisalDate = appraisalDate;
    }
}

class PerformanceCycle {
    public String cycleName;
    public String startDate;
    public String endDate;
    public String status;

    public PerformanceCycle(String cycleName, String startDate,
                             String endDate, String status) {
        this.cycleName = cycleName;
        this.startDate = startDate;
        this.endDate   = endDate;
        this.status    = status;
    }
}

class Feedback {
    public String feedbackId;
    public String employeeId;
    public String reviewerId;
    public String feedbackText;
    public String feedbackType;
    public String feedbackDate;

    public Feedback(String feedbackId, String employeeId, String reviewerId,
                    String feedbackText, String feedbackType, String feedbackDate) {
        this.feedbackId   = feedbackId;
        this.employeeId   = employeeId;
        this.reviewerId   = reviewerId;
        this.feedbackText = feedbackText;
        this.feedbackType = feedbackType;
        this.feedbackDate = feedbackDate;
    }
}

class KPI {
    public String kpiId;
    public String employeeId;
    public String goalId;
    public String kpiName;
    public float  targetValue;
    public float  actualValue;
    public String unit;

    public KPI(String kpiId, String employeeId, String goalId,
               String kpiName, float targetValue, float actualValue, String unit) {
        this.kpiId       = kpiId;
        this.employeeId  = employeeId;
        this.goalId      = goalId;
        this.kpiName     = kpiName;
        this.targetValue = targetValue;
        this.actualValue = actualValue;
        this.unit        = unit;
    }
}

class Reward {
    public String rewardId;
    public String employeeId;
    public String rewardType;
    public String description;
    public String awardedDate;

    public Reward(String rewardId, String employeeId, String rewardType,
                  String description, String awardedDate) {
        this.rewardId    = rewardId;
        this.employeeId  = employeeId;
        this.rewardType  = rewardType;
        this.description = description;
        this.awardedDate = awardedDate;
    }
}

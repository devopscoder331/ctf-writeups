package whatis.love.agedate.report

import whatis.love.agedate.api.model.MyProfile
import whatis.love.agedate.api.model.Profile

data class ReportDetails(
    val reporter: MyProfile,
    val reported: Profile,
    val reason: String,
)

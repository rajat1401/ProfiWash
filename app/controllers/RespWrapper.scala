package controllers

import com.bansal.washcatalog.catalog.common.Constants.X_ERROR_HEADER
import com.bansal.washcatalog.catalog.domain.{MyResponse, PWResp}
import play.api.libs.json.JsValue
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, Ok}
import utils.ObjectMapperUtil.objectMapper

object RespWrapper {
  implicit class RespWrapper(resp: MyResponse) {
    def withHeaders: Result = {
      resp.success match {
        case true => Ok(objectMapper.writeValueAsString(resp.data.getOrElse(Seq.empty)))
        case false => BadRequest.withHeaders((X_ERROR_HEADER, resp.error.getOrElse("Unknown error")))
      }
    }
  }
}



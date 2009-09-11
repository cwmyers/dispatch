package dispatch.mime

import org.apache.http.entity.mime.{FormBodyPart, MultipartEntity}
import org.apache.http.entity.mime.content.{FileBody, StringBody}

import java.io.File

/** Mime module for multipart form posting */
object Mime {
  /** Adds multipart operators to Request */
  implicit def Request2ExtendedRequest(r: Request) = new MimeRequest(r)

  /** Request derivative with multipart operators */
  class MimeRequest(r: Request) {
    /** Add file to multipart post, will convert other post methos to multipart */
    def <<= (name: String, file: File) = r.next {
      case post: MultipartPost => post.add(name, file)
      case p: Post[_] => r.mimic(new MultipartPost)(p).add(p.values).add(name, file)
      case req => r.mimic(new MultipartPost)(req).add(name, file)
    }
  }
}
// Not yet supported by Dispatch OAuth
class MultipartPost(val values: Map[String, Any], entity: MultipartEntity) extends Post[MultipartPost] {
  def this() = this(Map.empty, new MultipartEntity)
  def add(name: String, file: File) = {
    entity.addPart(name, new FileBody(file))
    this
  }
  setEntity(entity)
  def add(more: collection.Map[String, Any]) = {
    more.elements foreach { case (key, value) =>
      entity.addPart(key, new StringBody(value.toString))
    }
    new MultipartPost(values ++ more.elements, entity)
  }
}

package vn.com.fpt.sep490_g28_summer2024_be.utils;

public class EmailTemplate {
    public static String otpEmailTemplate = """
            <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
                <html xmlns="http://www.w3.org/1999/xhtml">
                <head>
                  <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Verify your login</title>
                  <!--[if mso]><style type="text/css">body, table, td, a { font-family: Arial, Helvetica, sans-serif !important; }</style><![endif]-->
                </head>
                <body style="font-family: Helvetica, Arial, sans-serif; margin: 0px; padding: 0px; background-color: #ffffff;">
                  <table role="presentation"
                    style="width: 100%%; border-collapse: collapse; border: 0px; border-spacing: 0px; font-family: Arial, Helvetica, sans-serif; background-color: rgb(239, 239, 239);">
                    <tbody>
                      <tr>
                        <td align="center" style="padding: 1rem 2rem; vertical-align: top; width: 100%%;">
                          <table role="presentation" style="max-width: 600px; border-collapse: collapse; border: 0px; border-spacing: 0px; text-align: left;">
                            <tbody>
                              <tr>
                                <td style="padding: 40px 0px 0px;">
                                  <div style="text-align: center;">
                                    <div style="padding-bottom: 20px;"><img src="https://i.imgur.com/fHfsLZq.png" alt="Góp Lẻ" style="width: 80px;"></div>
                                  </div>
                                  <div style="padding: 20px; background-color: rgb(255, 255, 255);">
                                    <div style="color: rgb(0, 0, 0); text-align: left;">
                                      <h1 style="margin: 1rem 0">Mã xác nhận</h1>
                                      <p style="padding-bottom: 16px">Kính gửi %s,</p>
                                      <p style="padding-bottom: 16px">Chúng tôi đã nhận được yêu cầu đăng ký tài khoản của bạn tại Góp Lẻ. Để hoàn tất quá trình
                                        đăng ký, vui lòng sử dụng mã OTP dưới đây:</p>
                                      <p style="padding-bottom: 16px"><strong style="font-size: 130%%">%s</strong></p>
                                      <p style="padding-bottom: 16px">Lưu ý:</p>
                                      <ul>
                                        <li>Mã OTP có hiệu lực trong vòng %d phút.</li>
                                        <li>Vui lòng không chia sẻ mã OTP này với bất kỳ ai.</li>
                                        <li>Nếu bạn không yêu cầu mã OTP, vui lòng bỏ qua email này hoặc liên hệ với bộ phận hỗ trợ của chúng tôi để được trợ
                                          giúp.</li>
                                      </ul>
                                      <p style="padding-bottom: 16px">Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi.</p>
                                      <p style="padding-bottom: 16px">Trân trọng,<br>Góp Lẻ</p>
                                      <p style="padding-bottom: 16px">Góp Lẻ<br><a href="mailto:niemtingroup@gmail.com" target="_blank"
                                      style="text-decoration: none;">niemtingroup@gmail.com</a><br>Điện thoại: 0975 302 307 | 0986 832 256<br>P702 - 62 Bà
                                    Triệu - TW Đoàn<br><a href="https://web.sucmanh2000.com" target="_blank"
                                      style="text-decoration: none;">https://web.sucmanh2000.com</a></p>
                                    </div>
                                  </div>
                                  <div style="padding-top: 20px; color: rgb(153, 153, 153); text-align: center;">
                                    <p style="padding-bottom: 16px">Góp Lẻ @ %d </p>
                                  </div>
                                </td>
                              </tr>
                            </tbody>
                          </table>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </body>
                </html>
            """;

    public static String resetPasswordEmailTemplate = """
            <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
            <html xmlns="http://www.w3.org/1999/xhtml">
                        
            <head>
              <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Reset your password</title>
              <!--[if mso]><style type="text/css">body, table, td, a { font-family: Arial, Helvetica, sans-serif !important; }</style><![endif]-->
            </head>
                        
            <body style="font-family: Helvetica, Arial, sans-serif; margin: 0px; padding: 0px; background-color: #ffffff;">
              <table role="presentation"
                style="width: 100%%; border-collapse: collapse; border: 0px; border-spacing: 0px; font-family: Arial, Helvetica, sans-serif; background-color: rgb(239, 239, 239);">
                <tbody>
                  <tr>
                    <td align="center" style="padding: 1rem 2rem; vertical-align: top; width: 100%%;">
                      <table role="presentation" style="max-width: 600px; border-collapse: collapse; border: 0px; border-spacing: 0px; text-align: left;">
                        <tbody>
                          <tr>
                            <td style="padding: 40px 0px 0px;">
                              <div style="text-align: center;">
                                <div style="padding-bottom: 20px;"><img src="https://i.imgur.com/fHfsLZq.png" alt="Góp Lẻ" style="width: 80px;"></div>
                              </div>
                              <div style="padding: 20px; background-color: rgb(255, 255, 255);">
                                <div style="color: rgb(0, 0, 0); text-align: left;">
                                  <p style="padding-bottom: 16px">Kính gửi %s,</p>
                                  <p style="padding-bottom: 16px">Chúng tôi xin thông báo rằng mật khẩu của bạn cho tài khoản [Tên Công ty/Ứng dụng] đã được reset thành công theo yêu cầu của bạn.</p>
                                  <p style="padding-bottom: 16px"><strong>Thông tin tài khoản:</strong></p>
                                  <ul>
                                    <li>Tên đăng nhập: %s</li>
                                  </ul>
                                  <p style="padding-bottom: 16px"><strong>Mật khẩu mới của bạn:</strong></p>
                                  <ul>
                                    <li>Mật khẩu mới: %s</li>
                                  </ul>
                                  <p style="padding-bottom: 16px">Vì lý do bảo mật, chúng tôi khuyên bạn nên đăng nhập vào tài khoản của mình ngay lập tức và thay đổi mật khẩu này thành một mật khẩu khác mà chỉ mình bạn biết. Bạn có thể thay đổi mật khẩu bằng cách làm theo các bước sau:</p>
                                  <ol>
                                    <li>Đăng nhập vào tài khoản của bạn tại [Liên kết đăng nhập].</li>
                                    <li>Truy cập vào phần "Đổi mật khẩu".</li>
                                    <li>Nhập mật khẩu hiện tại và mật khẩu mới mà bạn muốn sử dụng.</li>
                                    <li>Nhấn "Lưu thay đổi" để cập nhật mật khẩu của bạn.</li>
                                  </ol>
                                  <p style="padding-bottom: 16px">Nếu bạn không yêu cầu reset mật khẩu hoặc bạn có bất kỳ câu hỏi nào, vui lòng liên hệ qua Fanpage của dự án để được hỗ trợ tốt nhất.</p>
                                  <p style="padding-bottom: 16px">Chúng tôi xin lỗi vì bất kỳ sự bất tiện nào và cảm ơn bạn đã sử dụng dịch vụ của chúng tôi.</p>
                                  <p style="padding-bottom: 16px">Trân trọng,</p>
                                  <p style="padding-bottom: 16px">Góp Lẻ<br><a href="mailto:niemtingroup@gmail.com" target="_blank" style="text-decoration: none;">niemtingroup@gmail.com</a><br>Điện thoại: 0975 302 307 | 0986 832 256<br>P702 - 62 Bà Triệu - TW Đoàn<br><a href="https://web.sucmanh2000.com" target="_blank" style="text-decoration: none;">https://web.sucmanh2000.com</a></p>
                                </div>
                              </div>
                              <div style="padding-top: 20px; color: rgb(153, 153, 153); text-align: center;">
                                <p style="padding-bottom: 16px">GÓP LẺ @ %d</p>
                              </div>
                            </td>
                          </tr>
                        </tbody>
                      </table>
                    </td>
                  </tr>
                </tbody>
              </table>
            </body>
                        
            </html>
            """;

}

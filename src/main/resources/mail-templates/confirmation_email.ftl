<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registration Confirmation</title>
</head>
<body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
    <table role="presentation" style="width: 100%; border-collapse: collapse; background-color: #f4f4f4;">
        <tr>
            <td style="padding: 20px 0; text-align: center;">
                <table role="presentation" style="width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                    <tr>
                        <td style="padding: 30px; text-align: center; background-color: #ffffff; border-radius: 8px 8px 0 0;">
                            <img src="https://raw.githubusercontent.com/VladPiatachenko/MPF_Labs/lab7-freemaker/web/src/main/resources/static/img/logo.png"
                                 style="width:150px; margin-bottom:20px;" alt="BookApp">
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 0 30px 20px 30px;">
                            <div style="background-color: #e8f4f8; border-left: 4px solid #2196F3; padding: 15px; margin-bottom: 20px; border-radius: 4px;">
                                <h1 style="margin: 0; color: #1976D2; font-size: 24px; font-weight: bold;">Registration Confirmation</h1>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 0 30px 20px 30px;">
                            <p style="margin: 0 0 15px 0; color: #333333; font-size: 16px; line-height: 1.6;">
                                Hello, <strong>${firstName}</strong>!
                            </p>
                            <p style="margin: 0 0 15px 0; color: #333333; font-size: 16px; line-height: 1.6;">
                                Thank you for registering with Books Catalog.
                            </p>
                            <p style="margin: 0 0 20px 0; color: #333333; font-size: 16px; line-height: 1.6;">
                                To confirm your account, please click on the link below:
                            </p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 0 30px 20px 30px; text-align: center;">
                            <a href="${confirmationUrl}" 
                               style="display: inline-block; padding: 12px 30px; background-color: #2196F3; color: #ffffff; text-decoration: none; border-radius: 4px; font-weight: bold; font-size: 16px;">
                                Confirm Account
                            </a>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 0 30px 20px 30px;">
                            <div style="background-color: #ffffff; border: 1px solid #e0e0e0; padding: 20px; border-radius: 4px; margin-bottom: 20px;">
                                <p style="margin: 0 0 10px 0; color: #666666; font-size: 14px; font-weight: bold;">Or enter the confirmation code manually:</p>
                                <p style="margin: 0; color: #333333; font-size: 18px; font-weight: bold; text-align: center; letter-spacing: 2px; padding: 10px; background-color: #f5f5f5; border-radius: 4px;">
                                    ${confirmationCode}
                                </p>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 0 30px 20px 30px;">
                            <p style="margin: 0; color: #666666; font-size: 14px; line-height: 1.6;">
                                If you did not register with our service, please ignore this email.
                            </p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 20px 30px; text-align: center; background-color: #f5f5f5; border-radius: 0 0 8px 8px; border-top: 1px solid #e0e0e0;">
                            <p style="margin: 0; color: #666666; font-size: 12px;">Best regards,<br>The Books Catalog Team</p>
                            <p style="margin: 10px 0 0 0; color: #999999; font-size: 12px;">BookApp © 2025</p>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</body>
</html>


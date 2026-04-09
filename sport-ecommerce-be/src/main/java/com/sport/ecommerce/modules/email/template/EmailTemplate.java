package com.sport.ecommerce.modules.email.template;

import com.sport.ecommerce.modules.email.dto.EmailMessage;

public class EmailTemplate {

    public static String buildOrderTemplate(EmailMessage msg) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body {
                    font-family: Arial, sans-serif;
                    background-color: #f5f5f5;
                    padding: 20px;
                }
                .container {
                    background-color: #ffffff;
                    padding: 20px;
                    border-radius: 8px;
                }
                .header {
                    font-size: 20px;
                    font-weight: bold;
                    color: #333;
                }
                .content {
                    margin-top: 20px;
                }
                .order-box {
                    margin-top: 20px;
                    padding: 15px;
                    background-color: #fafafa;
                    border: 1px solid #ddd;
                }
                .total {
                    font-size: 18px;
                    font-weight: bold;
                    color: #e53935;
                }
                .footer {
                    margin-top: 30px;
                    font-size: 12px;
                    color: #999;
                }
                .btn {
                    display: inline-block;
                    padding: 10px 20px;
                    background-color: #1976d2;
                    color: #fff;
                    text-decoration: none;
                    border-radius: 4px;
                    margin-top: 20px;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">🎉 Order Confirmed!</div>

                <div class="content">
                    Hi <b>%s</b>,<br/><br/>

                    Thank you for your order. Your order has been successfully placed.

                    <div class="order-box">
                        <p><b>Order ID:</b> #%d</p>
                        <p class="total">Total: %s VND</p>
                    </div>

                    <a href="#" class="btn">View Order</a>
                </div>

                <div class="footer">
                    © 2026 Sport Ecommerce. All rights reserved.
                </div>
            </div>
        </body>
        </html>
        """.formatted(
                msg.getCustomerName(),
                msg.getOrderId(),
                msg.getTotalAmount()
        );
    }
}

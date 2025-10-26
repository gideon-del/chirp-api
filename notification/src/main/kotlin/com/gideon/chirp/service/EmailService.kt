package com.gideon.chirp.service

import com.gideon.chirp.domain.type.UserId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.time.Duration

@Service
class EmailService(
    private val emailTemplateService: EmailTemplateService,
    private val javaMailSender: JavaMailSender,
    @param:Value("\${chirp.email.from}")
    private val emailFrom: String,
    @param:Value("\${chirp.email.url}")
    private val baseUrl: String,
) {

    private val logger = LoggerFactory.getLogger(EmailService::class.java)
    fun sendVerificationEmail(
        email: String,
        username: String,
        userId: UserId,
        token: String
    ) {
        logger.info("Sending verification email to user $userId")
        val verificationUrl = UriComponentsBuilder
            .fromUriString("$baseUrl/api/auth/verify")
            .queryParam("token",token)
            .build()
            .toUriString()
        val htmlContent = emailTemplateService.processTemplate(
            templateName = "emails/account-verification",
            variables = mapOf(
                "username" to username,
                "verificationUrl" to verificationUrl
            )
        )

        sendHTMLEmail(
            to=email,
            subject = "Verify your Chirp account",
            html = htmlContent
        )
    }

    fun sendPasswordResetEmail(
        email: String,
        username: String,
        userId: UserId,
        token: String,
        expiration: Duration
    ) {
        logger.info("Sending password reset email to user $userId")
        val resetPasswordUrl = UriComponentsBuilder
            .fromUriString("$baseUrl/api/auth/reset-password")
            .queryParam("token",token)
            .build()
            .toUriString()
        val htmlContent = emailTemplateService.processTemplate(
            templateName = "emails/reset-password",
            variables = mapOf(
                "username" to username,
                "resetPasswordUrl" to resetPasswordUrl,
                "expiresInMinutes" to expiration.toMinutes()
            )
        )

        sendHTMLEmail(
            to=email,
            subject = "Reset your Chirp password",
            html = htmlContent
        )
    }
    private fun sendHTMLEmail(
        to:String,
        subject: String,
        html: String
    ){
        val message = javaMailSender.createMimeMessage(

        )
        MimeMessageHelper(message, true, "UTF-8").apply {
            setFrom(emailFrom)
            setTo(to)
            setSubject(subject)
            setText(html, true)
        }

        try {
            javaMailSender.send(message)
        }catch (e: MailException){
            logger.error("Could not send email", e)
        }
    }
}

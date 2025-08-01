package com.example.nagoyameshi.service;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.User;
import com.stripe.Stripe;
import com.stripe.exception.ApiConnectionException;
import com.stripe.exception.ApiException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.PermissionException;
import com.stripe.exception.RateLimitException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

@Service
public class StripeService {

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    @Value("${stripe.subscription.price-id}")
    private String subscriptionPriceId;
    
    // 決済成功時のリダイレクト先URL 
    @Value("${stripe.success-url}")
    private String stripeSuccessUrl;

    // 決済キャンセル時のリダイレクト先URL
    @Value("${stripe.cancel-url}")
    private String stripeCancelUrl; 
    
    @Value("${stripe.return-url}")
    private String stripeReturnUrl;

    private final UserService userService;

    public StripeService(UserService userService) {
        this.userService = userService;
    }

    public String createStripeSession(User user, HttpServletRequest request) {
        Stripe.apiKey = stripeApiKey;

        SessionCreateParams sessionCreateParams = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(stripeSuccessUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(stripeCancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice("price_1RnhSxQv6HLiC3Qyw3Py0OuL")
                                .setQuantity(1L)
                                .build())
                .putMetadata("userId", user.getId().toString())
                .build();

        try {
            // Stripeに送信する支払い情報をセッションとして作成する
            Session session = Session.create(sessionCreateParams);

            // 作成したセッションのIDを返す
            return session.getUrl();
            
        } catch (RateLimitException e) {
            System.out.println("短時間のうちに過剰な回数のAPIコールが行われました。");
            return "";
        } catch (InvalidRequestException e) {
            System.out.println("APIコールのパラメーターが誤っているか、状態が誤っているか、方法が無効でした。");
            return "";
        } catch (PermissionException e) {
            System.out.println("このリクエストに使用されたAPIキーには必要な権限がありません。");
            return "";
        } catch (AuthenticationException e) {
            System.out.println("Stripeは、提供された情報では認証できません。");
            return "";
        } catch (ApiConnectionException e) {
            System.out.println("お客様のサーバーとStripeの間でネットワークの問題が発生しました。");
            return "";
        } catch (ApiException e) {
            System.out.println("Stripe側で問題が発生しました（稀な状況です）。");
            return "";
        } catch (StripeException e) {
            System.out.println("Stripeとの通信中に予期せぬエラーが発生しました。");
            return "";
        }
    }

    // Webhookで呼び出され、ロールを変更
    public void processSessionCompleted(Event event) {
        Optional<StripeObject> optional = event.getDataObjectDeserializer().getObject();

        optional.ifPresent(stripeObject -> {
            Session session = (Session) stripeObject;
            String userId = session.getMetadata().get("userId");

            if (userId != null) {
                try {
                    // ユーザーを取得してロールを更新
                    Integer id = Integer.parseInt(userId);
                    userService.updateUserRole(id, "ROLE_PREMIUM");
                    System.out.println("ユーザーID " + userId + " をサブスク会員に変更しました");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public String createCustomerPortalUrl(User user) throws StripeException {
        Stripe.apiKey = stripeApiKey;

        SessionCreateParams params = SessionCreateParams.builder()
            .setCustomer(user.getStripeCustomerId())
            .setReturnUrl(stripeReturnUrl)
            .build();

        //Billing Portal用のSessionを生成
        Session session = Session.create(params);
        return session.getUrl();
    }
}

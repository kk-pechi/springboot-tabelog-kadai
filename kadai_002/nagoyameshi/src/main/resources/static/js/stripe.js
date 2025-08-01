const stripe = Stripe('pk_test_51RZUMvQv6HLiC3Qy1a45KbvTUwMDp2s6pLutPFOXUmRSnSXKP3GmbhK9sCWYRnM79QuHNw7kdoinLbaDB8MeC0dp00qodrAbp7');
const paymentButton = document.querySelector('#paymentButton');

paymentButton.addEventListener('click', () => {
 stripe.redirectToCheckout({
   sessionId: sessionId
 })
});
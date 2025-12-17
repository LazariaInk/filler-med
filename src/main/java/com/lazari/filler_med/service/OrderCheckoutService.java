package com.lazari.filler_med.service;

import com.lazari.filler_med.model.*;
import com.lazari.filler_med.repostiory.OrderRepository;
import com.stripe.model.PaymentIntent;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class OrderCheckoutService {

    private final CartService cartService;
    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final StripeService stripeService;

    public record CheckoutInput(
            String customerName,
            String customerEmail,
            String customerPhone,
            String shippingAddress,
            PaymentMethod paymentMethod
    ) {}

    public record CheckoutResult(
            Long orderId,
            String stripeClientSecret
    ) {}

    public OrderCheckoutService(CartService cartService,
                                ProductService productService,
                                OrderRepository orderRepository,
                                StripeService stripeService) {
        this.cartService = cartService;
        this.productService = productService;
        this.orderRepository = orderRepository;
        this.stripeService = stripeService;
    }

    @Transactional
    public CheckoutResult createOrderAndStartPayment(CheckoutInput in) {

        Cart cart = cartService.getCart();
        if (cart == null || cart.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Coșul este gol");
        }

        Order order = new Order();
        order.setStatus(OrderStatus.NEW);
        order.setPaymentMethod(in.paymentMethod());
        order.setCustomerName(clean(in.customerName()));
        order.setCustomerEmail(clean(in.customerEmail()));
        order.setCustomerPhone(clean(in.customerPhone()));
        order.setShippingAddress(clean(in.shippingAddress()));
        order.setCurrency("ron");

        int totalRon = 0;

        for (CartItem it : cart.getItemsList()) {
            Product p = productService.findById(it.getProductId());

            if (Boolean.FALSE.equals(p.getActive())) {
                throw new ResponseStatusException(BAD_REQUEST, "Produs inactiv: " + p.getTitle());
            }

            int stock = (p.getStock() == null) ? 0 : p.getStock();
            int qty = Math.max(it.getQuantity(), 1);
            if (qty > stock) {
                throw new ResponseStatusException(BAD_REQUEST, "Stoc insuficient pentru: " + p.getTitle());
            }

            int unitPrice = (p.getDiscountedPrice() != null) ? p.getDiscountedPrice() : p.getPrice();
            totalRon += unitPrice * qty;

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProductId(p.getId());
            oi.setTitle(p.getTitle());
            oi.setSlug(p.getSlug());
            oi.setImageUrl(p.getPrimaryImage() != null ? p.getPrimaryImage().getUrl() : "/images/logo1.png");
            oi.setUnitPriceRon(unitPrice);
            oi.setQuantity(qty);

            order.getItems().add(oi);
        }

        order.setTotalAmountRon(totalRon);

        if (in.paymentMethod() == PaymentMethod.COD) {
            order.setPaymentStatus(PaymentStatus.COD_PENDING);
            order.setStatus(OrderStatus.PLACED);
            Order saved = orderRepository.save(order);

            cartService.clear();

            return new CheckoutResult(saved.getId(), null);
        }

        if (in.paymentMethod() == PaymentMethod.STRIPE) {
            order.setPaymentStatus(PaymentStatus.PENDING);
            order.setStatus(OrderStatus.PENDING_PAYMENT);

            Order saved = orderRepository.save(order);

            long amountInCents = (long) saved.getTotalAmountRon() * 100L;
            PaymentIntent pi = stripeService.createPaymentIntent(amountInCents, "ron", saved.getId());

            saved.setStripePaymentIntentId(pi.getId());
            orderRepository.save(saved);

            cartService.clear();

            return new CheckoutResult(saved.getId(), pi.getClientSecret());
        }

        throw new ResponseStatusException(BAD_REQUEST, "Metodă de plată invalidă");
    }

    private String clean(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}

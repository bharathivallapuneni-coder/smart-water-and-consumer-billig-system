// Loads the Razorpay checkout script once and exposes a small helper
// that opens the payment sheet. In production, the "order" must be
// created server-side (Spring Boot) via the Razorpay Orders API so the
// amount can't be tampered with on the client — see comments below.

let scriptPromise = null

function loadRazorpayScript() {
  if (scriptPromise) return scriptPromise
  scriptPromise = new Promise((resolve, reject) => {
    if (window.Razorpay) return resolve(true)
    const script = document.createElement('script')
    script.src = 'https://checkout.razorpay.com/v1/checkout.js'
    script.onload = () => resolve(true)
    script.onerror = () => reject(new Error('Could not load Razorpay checkout'))
    document.body.appendChild(script)
  })
  return scriptPromise
}

/**
 * Opens Razorpay checkout for a bill.
 *
 * Real backend flow (Spring Boot):
 *   1. POST /api/payments/create-order  { billId }
 *      -> backend calls Razorpay Orders API, returns { orderId, amount, currency, keyId }
 *   2. Open checkout with that orderId (below).
 *   3. On success, Razorpay returns a signed payment id — send it to
 *      POST /api/payments/verify  { orderId, paymentId, signature }
 *      so the backend can verify the signature before marking the bill PAID.
 *
 * This helper accepts an `onSuccess`/`onFailure` pair so the calling page
 * doesn't need to know whether it's talking to the real gateway or a mock.
 */
export async function openRazorpayCheckout({ bill, resident, onSuccess, onFailure, mock = true }) {
  if (mock) {
    // Simulated gateway for local/demo use — swap `mock` to false once
    // VITE_RAZORPAY_KEY_ID and the backend order/verify endpoints exist.
    await new Promise((r) => setTimeout(r, 900))
    onSuccess({ paymentId: `pay_mock_${Date.now()}` })
    return
  }

  try {
    await loadRazorpayScript()
    const options = {
      key: import.meta.env.VITE_RAZORPAY_KEY_ID,
      amount: bill.amount * 100, // paise
      currency: 'INR',
      name: 'HydroBill',
      description: `Water bill · ${bill.month} ${bill.year} · Flat ${resident.flatNumber}`,
      // order_id: <from POST /api/payments/create-order>,
      handler: (response) => onSuccess(response),
      prefill: { name: resident.name, contact: resident.phone },
      theme: { color: '#4AA8D8' },
      modal: { ondismiss: () => onFailure?.(new Error('Payment cancelled')) }
    }
    const rzp = new window.Razorpay(options)
    rzp.on('payment.failed', (resp) => onFailure?.(new Error(resp.error.description)))
    rzp.open()
  } catch (err) {
    onFailure?.(err)
  }
}

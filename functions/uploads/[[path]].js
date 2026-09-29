const DEFAULT_BACKEND_ORIGIN = 'https://trace-occurred-api-obligations.trycloudflare.com'
const HOP_BY_HOP_RESPONSE_HEADERS = new Set([
  'content-length', 'content-encoding', 'transfer-encoding', 'connection',
  'keep-alive', 'proxy-authenticate', 'proxy-authorization', 'te', 'trailer', 'upgrade'
])

export async function onRequest(context) {
  const { request, env } = context
  const origin = env.BACKEND_ORIGIN || DEFAULT_BACKEND_ORIGIN
  const incomingUrl = new URL(request.url)
  const targetUrl = new URL(incomingUrl.pathname + incomingUrl.search, origin)

  try {
    const response = await fetch(targetUrl, {
      method: request.method,
      redirect: 'manual'
    })
    const headers = new Headers()
    for (const [key, value] of response.headers.entries()) {
      if (!HOP_BY_HOP_RESPONSE_HEADERS.has(key.toLowerCase())) {
        headers.set(key, value)
      }
    }
    return new Response(response.body, {
      status: response.status,
      statusText: response.statusText,
      headers
    })
  } catch (error) {
    return new Response('Backend service is unavailable', { status: 503 })
  }
}




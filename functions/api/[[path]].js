const DEFAULT_BACKEND_ORIGIN = 'https://immigrants-jackie-contribute-investigations.trycloudflare.com'
const HOP_BY_HOP_REQUEST_HEADERS = new Set([
  'host', 'origin', 'content-length', 'accept-encoding', 'connection', 'keep-alive',
  'proxy-connection', 'transfer-encoding', 'upgrade'
])
const HOP_BY_HOP_RESPONSE_HEADERS = new Set([
  'content-length', 'content-encoding', 'transfer-encoding', 'connection',
  'keep-alive', 'proxy-authenticate', 'proxy-authorization', 'te', 'trailer', 'upgrade'
])

function buildUrl(request, origin) {
  const incomingUrl = new URL(request.url)
  return new URL(incomingUrl.pathname + incomingUrl.search, origin)
}

function buildRequestHeaders(request) {
  const headers = new Headers()
  for (const [key, value] of request.headers.entries()) {
    const lowerKey = key.toLowerCase()
    if (!HOP_BY_HOP_REQUEST_HEADERS.has(lowerKey) && !lowerKey.startsWith('cf-')) {
      headers.set(key, value)
    }
  }
  return headers
}

function buildResponseHeaders(response) {
  const headers = new Headers()
  for (const [key, value] of response.headers.entries()) {
    if (!HOP_BY_HOP_RESPONSE_HEADERS.has(key.toLowerCase())) {
      headers.set(key, value)
    }
  }
  return headers
}

export async function onRequest(context) {
  const { request, env } = context
  const origin = env.BACKEND_ORIGIN || DEFAULT_BACKEND_ORIGIN
  const init = {
    method: request.method,
    headers: buildRequestHeaders(request),
    redirect: 'manual'
  }

  if (!['GET', 'HEAD'].includes(request.method)) {
    init.body = request.body
  }

  try {
    const response = await fetch(buildUrl(request, origin), init)
    return new Response(response.body, {
      status: response.status,
      statusText: response.statusText,
      headers: buildResponseHeaders(response)
    })
  } catch (error) {
    return new Response(JSON.stringify({ code: 503, msg: 'Backend service is unavailable' }), {
      status: 503,
      headers: { 'Content-Type': 'application/json;charset=UTF-8' }
    })
  }
}




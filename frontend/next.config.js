/** @type {import('next').NextConfig} */
const nextConfig = {
    publicRuntimeConfig: {
        apiUrl: process.env.GATEWAY_HOST + '/api'
    },
}

module.exports = nextConfig

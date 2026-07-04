import type { NextConfig } from "next";

const { hostname } = new URL(
  process.env.NEXT_PUBLIC_BASE_URL || "http://localhost",
);

const nextConfig: NextConfig = {
  /* config options here */
  experimental: {
    useCache: true,
  },

  images: {
    remotePatterns: [
      {
        protocol: process.env.NODE_ENV === "production" ? "https" : "http",
        hostname: hostname,
      },
    ],
  },
};

export default nextConfig;

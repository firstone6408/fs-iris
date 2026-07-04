import "@/styles/globals.css";
import type { Metadata } from "next";
import { Sarabun } from "next/font/google";

const sarabun = Sarabun({
  weight: ["400", "500", "600"],
  subsets: ["latin", "thai"],
});

export const metadata: Metadata = {
  metadataBase: new URL(
    process.env.NEXT_PUBLIC_BASE_URL ?? "http://localhost:3000",
  ),
  title: {
    default: "FS-Iris",
    template: "%s | FS-Iris",
  },
  description:
    "FS-Iris — a local-first AI assistant powered by large language models running on your own machine. Supports natural conversation, memory, and voice interaction.",
  openGraph: {
    title: "FS-Iris",
    description: "Local-first AI assistant with natural conversation and voice interaction.",
    siteName: "FS-Iris",
    images: [
      {
        url: "/images/logos/iris.png",
        width: 1200,
        height: 630,
        alt: "FS-Iris Banner",
      },
    ],
    locale: "th_TH",
    type: "website",
  },
  keywords: [
    "FS-Iris",
    "AI assistant",
    "local LLM",
    "Thai AI",
    "voice assistant",
    "text to speech",
    "speech to text",
    "offline AI",
  ],
  icons: "/images/logos/iris.png",
};

interface RootLayoutProps {
  children: React.ReactNode;
}

export default function RootLayout({ children }: Readonly<RootLayoutProps>) {
  return (
    <html lang="th">
      <body className={`${sarabun.className} antialiased`}>{children}</body>
    </html>
  );
}

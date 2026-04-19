import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "PushPlay Manager V2",
  description: "Système de gestion moderne pour cybercafé et salle de jeux — PushPlayManager V2",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="fr">
      <body>{children}</body>
    </html>
  );
}

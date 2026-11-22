import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Authentication - Atlant Dashboard",
  description: "Login or register to access the Atlant admin dashboard",
};

export default function AuthLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-zinc-50 to-zinc-100 dark:from-zinc-900 dark:to-black p-4">
      <div className="w-full max-w-md">
        {children}
      </div>
    </div>
  );
}

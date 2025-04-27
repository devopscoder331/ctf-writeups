import { Link } from "@heroui/link";

import { Head } from "./head";

import { Navbar } from "@/components/navbar";

export default function DefaultLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="relative flex flex-col h-screen">
      <Head />
      <Navbar />
      <main className="w-full mx-auto flex-grow">
        {children}
      </main>
      <footer className="w-full flex items-center justify-center py-3">
        <Navbar isRight />
      </footer>
    </div>
  );
}

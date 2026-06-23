import type { ReactNode } from "react";

import { Footer } from "./Footer";
import { Navbar } from "./Navbar";

interface LayoutProps {
  children: ReactNode;
}

export function Layout({ children }: LayoutProps) {
  return (
    <div className="app-layout">
      <Navbar />
      {children}
      <Footer />
    </div>
  );
}

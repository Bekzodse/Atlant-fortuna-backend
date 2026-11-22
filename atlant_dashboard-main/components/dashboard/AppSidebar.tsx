"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  LayoutDashboard,
  Package,
  Users,
  FileText,
  Image,
  Presentation,
  Settings,
  Send,
  Video,
} from "lucide-react";
import {
  Sidebar,
  SidebarContent,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarFooter,
} from "@/components/ui/sidebar";

const menuItems = [
  {
    title: "Dashboard",
    icon: LayoutDashboard,
    href: "/dashboard",
  },
  {
    title: "Products",
    icon: Package,
    href: "/dashboard/products",
  },
  {
    title: "Team Members",
    icon: Users,
    href: "/dashboard/team",
  },
  {
    title: "Licenses",
    icon: FileText,
    href: "/dashboard/licenses",
  },
  {
    title: "Gallery",
    icon: Image,
    href: "/dashboard/gallery",
  },
  {
    title: "Reels",
    icon: Video,
    href: "/dashboard/reels",
  },
  {
    title: "Carousel",
    icon: Presentation,
    href: "/dashboard/carousel",
  },
  {
    title: "Telegram",
    icon: Send,
    href: "/dashboard/telegram",
  },
  {
    title: "Settings",
    icon: Settings,
    href: "/dashboard/settings",
  },
];

export function AppSidebar() {
  const pathname = usePathname();

  return (
    <Sidebar>
      <SidebarHeader className="border-b p-6">
        <Link href="/dashboard" className="flex items-center gap-2">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
            <span className="text-lg font-bold">A</span>
          </div>
          <span className="text-lg font-semibold">Atlant</span>
        </Link>
      </SidebarHeader>
      <SidebarContent>
        <SidebarMenu>
          {menuItems.map((item) => {
            const isActive = pathname === item.href;
            return (
              <SidebarMenuItem key={item.href}>
                <SidebarMenuButton
                  asChild
                  isActive={isActive}
                  tooltip={item.title}
                >
                  <Link href={item.href}>
                    <item.icon className="h-4 w-4" />
                    <span>{item.title}</span>
                  </Link>
                </SidebarMenuButton>
              </SidebarMenuItem>
            );
          })}
        </SidebarMenu>
      </SidebarContent>
      <SidebarFooter className="border-t p-4">
        <div className="text-xs text-muted-foreground text-center">
          Atlant Dashboard v1.0
        </div>
      </SidebarFooter>
    </Sidebar>
  );
}

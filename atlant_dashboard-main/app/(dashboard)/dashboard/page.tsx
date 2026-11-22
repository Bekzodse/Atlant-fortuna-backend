"use client";

import { useMemo } from "react";
import { Package, Users, FileText, Image } from "lucide-react";
import { StatsCard } from "@/components/dashboard/StatsCard";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useDashboardStats } from "@/hooks/use-api";
import {
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";

const COLORS = ["#0088FE", "#00C49F", "#FFBB28", "#FF8042", "#8884D8", "#82CA9D"];

export default function DashboardPage() {
  const { stats, isLoading } = useDashboardStats();

  const formatCategoryName = (category: string) => {
    return category
      .split("_")
      .map((word) => word.charAt(0) + word.slice(1).toLowerCase())
      .join(" ");
  };

  const categoryData = useMemo(() => {
    return Object.entries(stats.productsByCategory).map(([name, value]) => ({
      name: formatCategoryName(name),
      value,
    }));
  }, [stats.productsByCategory]);

  const discountData = [
    { name: "With Discount", value: stats.productsWithDiscount },
    {
      name: "Regular Price",
      value: stats.totalProducts - stats.productsWithDiscount,
    },
  ];

  if (isLoading) {
    return (
      <div className="space-y-6">
        <h1 className="text-3xl font-bold">Dashboard</h1>
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          {[1, 2, 3, 4].map((i) => (
            <Card key={i}>
              <CardHeader className="pb-2">
                <Skeleton className="h-4 w-24" />
              </CardHeader>
              <CardContent>
                <Skeleton className="h-8 w-16" />
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Dashboard</h1>
        <p className="text-muted-foreground">
          Welcome to Atlant admin dashboard
        </p>
      </div>

      {/* Stats Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatsCard
          title="Total Products"
          value={stats.totalProducts}
          icon={Package}
          description={`${stats.productsWithDiscount} with discounts`}
        />
        <StatsCard
          title="Team Members"
          value={stats.totalTeamMembers}
          icon={Users}
          description="Active members"
        />
        <StatsCard
          title="Licenses"
          value={stats.totalLicenses}
          icon={FileText}
          description="Total licenses"
        />
        <StatsCard
          title="Gallery Images"
          value={stats.totalGalleryImages}
          icon={Image}
          description="Total images"
        />
      </div>

      {/* Charts */}
      <div className="space-y-4">
        <Card className="w-full">
          <CardHeader>
            <CardTitle>Products by Category</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={categoryData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, percent }) =>
                    `${name}: ${((percent || 0) * 100).toFixed(0)}%`
                  }
                  outerRadius={80}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {categoryData.map((entry, index) => (
                    <Cell
                      key={`cell-${index}`}
                      fill={COLORS[index % COLORS.length]}
                    />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card className="w-full">
          <CardHeader>
            <CardTitle>Products Discount Distribution</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={discountData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="value" fill="#8884d8" />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      {/* Quick Actions */}
      <Card>
        <CardHeader>
          <CardTitle>Quick Actions</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-2 md:grid-cols-2 lg:grid-cols-4">
            <a
              href="/dashboard/products/new"
              className="p-4 border rounded-lg hover:bg-accent transition-colors"
            >
              <Package className="h-5 w-5 mb-2" />
              <div className="font-medium">Add Product</div>
              <div className="text-sm text-muted-foreground">
                Create new product
              </div>
            </a>
            <a
              href="/dashboard/team/new"
              className="p-4 border rounded-lg hover:bg-accent transition-colors"
            >
              <Users className="h-5 w-5 mb-2" />
              <div className="font-medium">Add Team Member</div>
              <div className="text-sm text-muted-foreground">
                Add new member
              </div>
            </a>
            <a
              href="/dashboard/licenses/new"
              className="p-4 border rounded-lg hover:bg-accent transition-colors"
            >
              <FileText className="h-5 w-5 mb-2" />
              <div className="font-medium">Add License</div>
              <div className="text-sm text-muted-foreground">
                Upload new license
              </div>
            </a>
            <a
              href="/dashboard/gallery"
              className="p-4 border rounded-lg hover:bg-accent transition-colors"
            >
              <Image className="h-5 w-5 mb-2" />
              <div className="font-medium">Upload Images</div>
              <div className="text-sm text-muted-foreground">
                Add to gallery
              </div>
            </a>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

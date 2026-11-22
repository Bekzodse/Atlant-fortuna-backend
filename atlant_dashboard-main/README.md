# Atlant Dashboard

A modern, full-featured admin dashboard for managing Atlant's water filtration equipment business.

## Features

### 🔐 Authentication
- User registration and login
- JWT-based authentication
- Protected routes with automatic token management
- Secure logout functionality

### 📊 Dashboard Home
- Real-time statistics cards (Products, Team Members, Licenses, Gallery Images)
- Interactive charts (Products by Category, Discount Distribution)
- Quick action shortcuts
- Beautiful data visualization with Recharts

### 🛍️ Product Management
- Full CRUD operations for products
- Multi-language support (English, Russian, Uzbek)
- Product categorization (6 categories)
- Multiple image uploads per product
- Discount tracking
- Search and filter functionality
- Rich product details (price, brand, article number, delivery info)

### 👥 Team Member Management
- Add, edit, and delete team members
- Multi-language names and roles
- Profile photo uploads
- Phone number tracking
- Card-based grid layout

### 📄 License Management
- Upload and manage company licenses
- Multi-language license titles
- Image-based license documents
- Quick delete functionality

### 🖼️ Gallery Management
- Upload multiple photos
- Grid view with hover effects
- Bulk photo uploads
- Individual photo deletion

### 🎠 Carousel Management
- Manage homepage carousel slides
- Multi-language titles and descriptions
- Toggle active/inactive status
- Image uploads for slides

## Tech Stack

### Frontend Framework
- **Next.js 16.0.7** - React framework with App Router
- **React 19.2.0** - Latest React version
- **TypeScript 5** - Type-safe development

### Styling
- **Tailwind CSS v4** - Latest utility-first CSS framework
- **shadcn/ui** - Beautiful, accessible component library
- **Radix UI** - Unstyled, accessible UI primitives

### State Management & Forms
- **Zustand** - Lightweight state management for auth
- **React Hook Form** - Performant form handling
- **Zod** - TypeScript-first schema validation

### Data Visualization
- **Recharts** - Composable charting library for React

### API Communication
- **Axios** - Promise-based HTTP client
- RESTful API integration
- Automatic token injection
- Error handling with interceptors

## Getting Started

### Prerequisites
- Node.js 18+
- npm or yarn

### Installation

1. Install dependencies:
```bash
npm install
```

2. Run the development server:
```bash
npm run dev
```

3. Open [http://localhost:3000](http://localhost:3000) in your browser

### Build for Production

```bash
npm run build
npm run start
```

## API Integration

### Base URL
```
https://atlant-backend-production.up.railway.app/api/v1
```

### Authentication
- Bearer Token (JWT) stored in localStorage
- Automatic token injection via Axios interceptors
- Auto-redirect to login on 401 responses

## Key Features

### Multi-Language Support
All content fields support three languages (EN/RU/UZ) via reusable `MultiLangInput` and `MultiLangTextarea` components.

### Image Uploads
- Drag-and-drop interface
- Multiple image support
- Image preview before upload
- Integration with backend attachment API

### Responsive Design
- Mobile-first approach
- Collapsible sidebar
- Touch-friendly interface

---

Built with ❤️ using Next.js 16, Tailwind CSS v4, and shadcn/ui

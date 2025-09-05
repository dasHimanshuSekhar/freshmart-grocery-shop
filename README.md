# FreshMart - Grocery Shop Application

A comprehensive grocery shop application built with Next.js (React) frontend and Node.js backend, featuring user authentication, product management, shopping cart, and order management.

## 🚀 Features

### Customer Features
- **User Authentication**: OTP-based registration and login
- **Product Browsing**: Browse products by categories with search functionality
- **Shopping Cart**: Add/remove items, update quantities
- **Order Management**: Place orders and track order history
- **Responsive Design**: Works on desktop and mobile devices

### Admin Features
- **Dashboard**: Overview of orders, products, and categories
- **Product Management**: Add, edit, and delete products
- **Category Management**: Manage product categories
- **Order Management**: View and update order status
- **User Management**: View registered users

## 🛠️ Tech Stack

### Frontend
- **Next.js 15** - React framework with App Router
- **TypeScript** - Type-safe development
- **Tailwind CSS** - Utility-first CSS framework
- **shadcn/ui** - Modern UI components
- **Lucide React** - Beautiful icons
- **Sonner** - Toast notifications

### Backend
- **Node.js** - JavaScript runtime
- **Express.js** - Web framework
- **In-memory Database** - For demo purposes (easily replaceable with PostgreSQL/MongoDB)
- **Nodemailer** - Email functionality
- **CORS** - Cross-origin resource sharing

## 📦 Installation

### Prerequisites
- Node.js (v18 or higher)
- npm or bun package manager

### Clone the Repository
```bash
git clone https://github.com/yourusername/freshmart-grocery-shop.git
cd freshmart-grocery-shop
```

### Backend Setup
```bash
cd grocery-backend-node
npm install
npm start
```
The backend will run on `http://localhost:8080`

### Frontend Setup
```bash
cd grocery-frontend
bun install
bun run dev
```
The frontend will run on `http://localhost:3000`

## 🎯 Usage

### For Customers
1. **Register/Login**: Use OTP-based authentication
2. **Browse Products**: Explore different categories
3. **Add to Cart**: Select products and quantities
4. **Place Order**: Complete your purchase
5. **Track Orders**: Monitor order status

### For Admin
1. **Admin Registration**: First admin is auto-created
2. **Manage Products**: Add/edit/delete products
3. **Manage Categories**: Organize products
4. **Process Orders**: Update order status
5. **View Analytics**: Monitor business metrics

## 📱 Screenshots

### Customer Dashboard
- Product browsing with categories
- Shopping cart functionality
- Order history tracking

### Admin Dashboard
- Order management
- Product and category management
- User analytics

## 🔧 Configuration

### Environment Variables
Create a `.env.local` file in the frontend directory:
```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

### Email Configuration
Update the nodemailer configuration in `grocery-backend-node/server.js` for production email sending.

## 🚀 Deployment

### Frontend (Vercel)
```bash
cd grocery-frontend
vercel --prod
```

### Backend (Railway/Heroku)
```bash
cd grocery-backend-node
# Deploy to your preferred platform
```

## 🔮 Future Enhancements

- **Payment Gateway Integration**: Stripe/Razorpay integration
- **Real-time Notifications**: WebSocket for order updates
- **Delivery Tracking**: GPS-based delivery tracking
- **Inventory Management**: Advanced stock management
- **Analytics Dashboard**: Business intelligence features
- **Mobile App**: React Native mobile application

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Himanshu Sekhar Das**
- Email: dashimanshusekhar58@gmail.com
- GitHub: [@himanshusekhar](https://github.com/himanshusekhar)

## 🙏 Acknowledgments

- [Next.js](https://nextjs.org/) for the amazing React framework
- [shadcn/ui](https://ui.shadcn.com/) for beautiful UI components
- [Tailwind CSS](https://tailwindcss.com/) for utility-first styling
- [Lucide](https://lucide.dev/) for the icon library

---

⭐ If you found this project helpful, please give it a star!

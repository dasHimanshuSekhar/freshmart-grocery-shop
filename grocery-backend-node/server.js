const express = require('express');
const cors = require('cors');
const { v4: uuidv4 } = require('uuid');
const nodemailer = require('nodemailer');

const app = express();
const PORT = process.env.PORT || 8080;

// Middleware
app.use(cors());
app.use(express.json());

// In-memory database (for demo purposes)
const database = {
  users: new Map(),
  categories: new Map(),
  products: new Map(),
  orders: new Map(),
  otps: new Map()
};

// Email configuration (mock for demo)
const transporter = nodemailer.createTransport({
  host: 'smtp.gmail.com',
  port: 587,
  secure: false,
  auth: {
    user: 'demo@example.com',
    pass: 'demo-password'
  }
});

// Utility functions
const generateOTP = () => Math.floor(100000 + Math.random() * 900000).toString();

const sendEmail = async (to, subject, text) => {
  console.log(`Email sent to ${to}: ${subject} - ${text}`);
  // In production, use actual email sending
  return true;
};

// Auth Routes
app.post('/api/auth/register-admin', (req, res) => {
  try {
    const { email, name, phone } = req.body;
    
    // Check if admin already exists
    const existingAdmin = Array.from(database.users.values()).find(user => user.role === 'ADMIN');
    if (existingAdmin) {
      return res.status(400).json({ success: false, message: 'Admin already exists' });
    }

    const adminId = uuidv4();
    const admin = {
      id: adminId,
      email,
      name,
      phone,
      role: 'ADMIN',
      isVerified: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    database.users.set(adminId, admin);

    res.json({
      success: true,
      message: 'Admin registered successfully',
      data: { userId: adminId, email, name, role: 'ADMIN' }
    });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Registration failed: ' + error.message });
  }
});

app.post('/api/auth/send-otp', (req, res) => {
  try {
    const { email } = req.body;
    const otp = generateOTP();
    
    // Store OTP with 5-minute expiry
    database.otps.set(email, { otp, expires: Date.now() + 5 * 60 * 1000 });
    
    // Send email (mock)
    sendEmail(email, 'Grocery Shop - Login OTP', `Your OTP is: ${otp}`);
    
    res.json({
      success: true,
      message: 'OTP sent successfully',
      data: otp // For demo purposes only
    });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Failed to send OTP: ' + error.message });
  }
});

app.post('/api/auth/register', (req, res) => {
  try {
    const { email, name, phone, otp } = req.body;
    
    // Verify OTP
    const storedOtp = database.otps.get(email);
    if (!storedOtp || storedOtp.otp !== otp || storedOtp.expires < Date.now()) {
      return res.status(400).json({ success: false, message: 'Invalid or expired OTP' });
    }

    // Check if user already exists
    const existingUser = Array.from(database.users.values()).find(user => user.email === email);
    if (existingUser) {
      return res.status(400).json({ success: false, message: 'User already exists' });
    }

    const userId = uuidv4();
    const user = {
      id: userId,
      email,
      name,
      phone,
      role: 'CUSTOMER',
      isVerified: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    database.users.set(userId, user);
    database.otps.delete(email);

    res.json({
      success: true,
      message: 'Registration successful',
      data: { userId, email, name, role: 'CUSTOMER' }
    });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Registration failed: ' + error.message });
  }
});

app.post('/api/auth/login', (req, res) => {
  try {
    const { email, otp } = req.body;
    
    // Verify OTP
    const storedOtp = database.otps.get(email);
    if (!storedOtp || storedOtp.otp !== otp || storedOtp.expires < Date.now()) {
      return res.status(400).json({ success: false, message: 'Invalid or expired OTP' });
    }

    // Find user
    const user = Array.from(database.users.values()).find(user => user.email === email);
    if (!user) {
      return res.status(400).json({ success: false, message: 'User not found' });
    }

    database.otps.delete(email);

    res.json({
      success: true,
      message: 'Login successful',
      data: { userId: user.id, email: user.email, name: user.name, role: user.role }
    });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Login failed: ' + error.message });
  }
});

// Category Routes
app.get('/api/categories', (req, res) => {
  try {
    const categories = Array.from(database.categories.values());
    res.json({ success: true, message: 'Categories retrieved successfully', data: categories });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Failed to retrieve categories: ' + error.message });
  }
});

app.post('/api/categories', (req, res) => {
  try {
    const categoryData = req.body;
    const categoryId = uuidv4();
    
    const category = {
      id: categoryId,
      ...categoryData,
      isActive: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    database.categories.set(categoryId, category);

    res.json({
      success: true,
      message: 'Category created successfully',
      data: category
    });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Failed to create category: ' + error.message });
  }
});

// Product Routes
app.get('/api/products', (req, res) => {
  try {
    let products = Array.from(database.products.values());
    
    const { categoryId, search } = req.query;
    
    if (categoryId) {
      products = products.filter(product => product.categoryId === categoryId);
    }
    
    if (search) {
      const searchLower = search.toLowerCase();
      products = products.filter(product => 
        product.name.toLowerCase().includes(searchLower) ||
        product.description.toLowerCase().includes(searchLower)
      );
    }
    
    res.json({ success: true, message: 'Products retrieved successfully', data: products });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Failed to retrieve products: ' + error.message });
  }
});

app.post('/api/products', (req, res) => {
  try {
    const productData = req.body;
    const productId = uuidv4();
    
    const product = {
      id: productId,
      ...productData,
      isActive: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    database.products.set(productId, product);

    res.json({
      success: true,
      message: 'Product created successfully',
      data: product
    });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Failed to create product: ' + error.message });
  }
});

app.delete('/api/products/:id', (req, res) => {
  try {
    const { id } = req.params;
    
    if (database.products.delete(id)) {
      res.json({ success: true, message: 'Product deleted successfully' });
    } else {
      res.status(404).json({ success: false, message: 'Product not found' });
    }
  } catch (error) {
    res.status(500).json({ success: false, message: 'Failed to delete product: ' + error.message });
  }
});

// Order Routes
app.get('/api/orders', (req, res) => {
  try {
    let orders = Array.from(database.orders.values());
    
    const { customerId, status } = req.query;
    
    if (customerId) {
      orders = orders.filter(order => order.customerId === customerId);
    }
    
    if (status) {
      orders = orders.filter(order => order.status === status);
    }
    
    res.json({ success: true, message: 'Orders retrieved successfully', data: orders });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Failed to retrieve orders: ' + error.message });
  }
});

app.get('/api/orders/customer/:customerId', (req, res) => {
  try {
    const { customerId } = req.params;
    const orders = Array.from(database.orders.values()).filter(order => order.customerId === customerId);
    
    res.json({ success: true, message: 'Customer orders retrieved successfully', data: orders });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Failed to retrieve customer orders: ' + error.message });
  }
});

app.post('/api/orders', (req, res) => {
  try {
    const orderData = req.body;
    const orderId = uuidv4();
    
    const order = {
      id: orderId,
      ...orderData,
      status: 'PENDING',
      orderDate: new Date().toISOString()
    };

    database.orders.set(orderId, order);

    // Send notification to admin
    const admin = Array.from(database.users.values()).find(user => user.role === 'ADMIN');
    if (admin) {
      const orderDetails = `New order from ${order.customerEmail}\nTotal: ₹${order.totalAmount}\nItems: ${order.items.length}`;
      sendEmail(admin.email, 'New Order Received', orderDetails);
    }

    res.json({
      success: true,
      message: 'Order created successfully',
      data: order
    });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Failed to create order: ' + error.message });
  }
});

app.put('/api/orders/:id/status', (req, res) => {
  try {
    const { id } = req.params;
    const { status, deliveryTime } = req.body;
    
    const order = database.orders.get(id);
    if (!order) {
      return res.status(404).json({ success: false, message: 'Order not found' });
    }

    order.status = status;
    order.updatedAt = new Date().toISOString();
    
    if (deliveryTime) {
      order.deliveryTime = deliveryTime;
    }

    database.orders.set(id, order);

    // Send confirmation email to customer if order is confirmed
    if (status === 'CONFIRMED') {
      const orderDetails = `Your order #${id.slice(-8)} has been confirmed!\nTotal: ₹${order.totalAmount}\nEstimated delivery: ${deliveryTime || '2-3 hours'}`;
      sendEmail(order.customerEmail, 'Order Confirmed', orderDetails);
    }

    res.json({
      success: true,
      message: 'Order status updated successfully',
      data: order
    });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Failed to update order status: ' + error.message });
  }
});

// Initialize with sample data
const initializeSampleData = () => {
  // Sample categories
  const categories = [
    { name: 'Fruits', description: 'Fresh seasonal fruits', imageUrl: 'https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=400' },
    { name: 'Vegetables', description: 'Farm fresh vegetables', imageUrl: 'https://images.unsplash.com/photo-1540420773420-3366772f4999?w=400' },
    { name: 'Dairy', description: 'Milk, cheese, and dairy products', imageUrl: 'https://images.unsplash.com/photo-1563636619-e9143da7973b?w=400' },
    { name: 'Snacks', description: 'Healthy and tasty snacks', imageUrl: 'https://images.unsplash.com/photo-1599490659213-e2b9527bd087?w=400' }
  ];

  categories.forEach(cat => {
    const id = uuidv4();
    database.categories.set(id, {
      id,
      ...cat,
      isActive: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    });
  });

  // Sample products
  const categoryIds = Array.from(database.categories.keys());
  const products = [
    { name: 'Fresh Apples', description: 'Crisp red apples', price: 120, categoryId: categoryIds[0], stockQuantity: 50, unit: 'kg', imageUrl: 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400' },
    { name: 'Bananas', description: 'Ripe yellow bananas', price: 60, categoryId: categoryIds[0], stockQuantity: 30, unit: 'dozen', imageUrl: 'https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=400' },
    { name: 'Tomatoes', description: 'Fresh red tomatoes', price: 40, categoryId: categoryIds[1], stockQuantity: 25, unit: 'kg', imageUrl: 'https://images.unsplash.com/photo-1546470427-e26264be0b0d?w=400' },
    { name: 'Onions', description: 'Fresh onions', price: 30, categoryId: categoryIds[1], stockQuantity: 40, unit: 'kg', imageUrl: 'https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=400' },
    { name: 'Milk', description: 'Fresh cow milk', price: 60, categoryId: categoryIds[2], stockQuantity: 20, unit: 'liter', imageUrl: 'https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400' },
    { name: 'Cheese', description: 'Processed cheese', price: 200, categoryId: categoryIds[2], stockQuantity: 15, unit: 'pack', imageUrl: 'https://images.unsplash.com/photo-1486297678162-eb2a19b0a32d?w=400' }
  ];

  products.forEach(prod => {
    const id = uuidv4();
    database.products.set(id, {
      id,
      ...prod,
      isActive: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    });
  });

  console.log('Sample data initialized');
};

// Start server
app.listen(PORT, () => {
  console.log(`Grocery Shop Backend running on port ${PORT}`);
  initializeSampleData();
});

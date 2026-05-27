import React, { useState, useEffect } from 'react';
import { apiService } from './services/api';
import { ShoppingCart, Plus, Minus, X, CheckCircle, Search, ArrowLeft } from 'lucide-react';

const CustomerOrderApp = () => {
  const [tableId, setTableId] = useState('');
  const [tableName, setTableName] = useState('');
  
  const [categories, setCategories] = useState([]);
  const [foods, setFoods] = useState([]);
  const [cart, setCart] = useState([]);
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const [activeCategory, setActiveCategory] = useState(null);
  const [showCart, setShowCart] = useState(false);
  const [orderStatus, setOrderStatus] = useState(null); // null, 'success', 'error'

  useEffect(() => {
    // Read table info from URL
    const params = new URLSearchParams(window.location.search);
    const tid = params.get('tableId');
    const tname = params.get('tableName');
    
    if (tid) setTableId(tid);
    if (tname) setTableName(tname);
    
    fetchMenu();
  }, []);

  const fetchMenu = async () => {
    try {
      const [catsRes, foodsRes] = await Promise.all([
        apiService.catalog.getCategories(),
        apiService.catalog.getItems(true) // Get only active items
      ]);
      setCategories(catsRes.data || []);
      setFoods(foodsRes.data || []);
      
      if (catsRes.data && catsRes.data.length > 0) {
        setActiveCategory(catsRes.data[0].id);
      }
    } catch (err) {
      setError('Lỗi khi tải thực đơn. Vui lòng thử lại.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const addToCart = (food) => {
    setCart(prev => {
      const existing = prev.find(item => item.menuItemId === food.id);
      if (existing) {
        return prev.map(item => 
          item.menuItemId === food.id 
            ? { ...item, quantity: item.quantity + 1 } 
            : item
        );
      }
      return [...prev, {
        menuItemId: food.id,
        foodName: food.foodName,
        unitPrice: food.price,
        quantity: 1,
        note: '',
        image: food.imageUrl
      }];
    });
  };

  const updateQuantity = (menuItemId, delta) => {
    setCart(prev => {
      return prev.map(item => {
        if (item.menuItemId === menuItemId) {
          const newQty = item.quantity + delta;
          return newQty > 0 ? { ...item, quantity: newQty } : item;
        }
        return item;
      }).filter(item => item.quantity > 0);
    });
  };

  const getTotal = () => {
    return cart.reduce((sum, item) => sum + (item.unitPrice * item.quantity), 0);
  };

  const submitOrder = async () => {
    if (cart.length === 0) return;
    setLoading(true);
    try {
      const payload = {
        tableId: tableId || null,
        tableNumber: tableName || 'Mang đi',
        note: 'Order từ mã QR',
        items: cart.map(item => ({
          menuItemId: item.menuItemId,
          foodName: item.foodName,
          unitPrice: item.unitPrice,
          quantity: item.quantity,
          note: item.note
        }))
      };
      
      // We will need to add createPublic to api.js
      await apiService.order.createPublic(payload);
      
      setOrderStatus('success');
      setCart([]);
      setShowCart(false);
    } catch (err) {
      console.error(err);
      alert('Đã xảy ra lỗi khi đặt món: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  if (loading && foods.length === 0) {
    return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>Đang tải thực đơn...</div>;
  }

  if (orderStatus === 'success') {
    return (
      <div style={{ padding: '40px 20px', textAlign: 'center', maxWidth: '500px', margin: '0 auto' }}>
        <CheckCircle size={64} color="#10B981" style={{ margin: '0 auto 20px' }} />
        <h2 style={{ fontSize: '24px', fontWeight: '800', marginBottom: '10px' }}>Đặt món thành công!</h2>
        <p style={{ color: '#64748B', marginBottom: '30px' }}>Bếp đã nhận order của bạn. Vui lòng chờ trong giây lát.</p>
        <button 
          onClick={() => setOrderStatus(null)}
          style={{ width: '100%', padding: '16px', backgroundColor: '#11117F', color: 'white', border: 'none', borderRadius: '12px', fontSize: '16px', fontWeight: '700' }}
        >
          Tiếp tục xem Menu
        </button>
      </div>
    );
  }

  const activeFoods = foods.filter(f => f.categoryId === activeCategory);

  return (
    <div style={{ backgroundColor: '#F8FAFC', minHeight: '100vh', paddingBottom: '80px', fontFamily: '"Inter", sans-serif' }}>
      {/* Header */}
      <div style={{ backgroundColor: '#11117F', padding: '20px', color: 'white', position: 'sticky', top: 0, zIndex: 10 }}>
        <h1 style={{ margin: 0, fontSize: '20px', fontWeight: '800' }}>NHÀ HÀNG FOOD</h1>
        {tableName ? (
          <p style={{ margin: '5px 0 0 0', opacity: 0.8, fontSize: '14px' }}>Bạn đang ngồi tại: <strong>Bàn {tableName}</strong></p>
        ) : (
          <p style={{ margin: '5px 0 0 0', opacity: 0.8, fontSize: '14px' }}>Chào mừng quý khách</p>
        )}
      </div>

      {/* Categories */}
      <div style={{ display: 'flex', overflowX: 'auto', padding: '16px', gap: '10px', backgroundColor: 'white', borderBottom: '1px solid #E2E8F0', position: 'sticky', top: '70px', zIndex: 9 }}>
        {categories.map(cat => (
          <button
            key={cat.id}
            onClick={() => setActiveCategory(cat.id)}
            style={{
              padding: '8px 16px',
              borderRadius: '20px',
              border: 'none',
              backgroundColor: activeCategory === cat.id ? '#11117F' : '#F1F5F9',
              color: activeCategory === cat.id ? 'white' : '#64748B',
              fontWeight: '600',
              whiteSpace: 'nowrap',
              fontSize: '14px',
              cursor: 'pointer'
            }}
          >
            {cat.categoryName}
          </button>
        ))}
      </div>

      {/* Menu Items */}
      <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {activeFoods.map(food => {
          const inCart = cart.find(c => c.menuItemId === food.id);
          return (
            <div key={food.id} style={{ display: 'flex', backgroundColor: 'white', borderRadius: '16px', padding: '12px', gap: '12px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
              {food.imageUrl ? (
                <img src={`http://localhost:8081${food.imageUrl}`} alt={food.foodName} style={{ width: '90px', height: '90px', borderRadius: '12px', objectFit: 'cover' }} />
              ) : (
                <div style={{ width: '90px', height: '90px', borderRadius: '12px', backgroundColor: '#E2E8F0', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#94A3B8' }}>No Img</div>
              )}
              
              <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
                <div>
                  <h3 style={{ margin: 0, fontSize: '16px', fontWeight: '700', color: '#1E293B' }}>{food.foodName}</h3>
                  <p style={{ margin: '4px 0 0 0', fontSize: '14px', fontWeight: '800', color: '#11117F' }}>
                    {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(food.price)}
                  </p>
                </div>
                
                <div style={{ alignSelf: 'flex-end' }}>
                  {inCart ? (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px', backgroundColor: '#F1F5F9', padding: '4px', borderRadius: '20px' }}>
                      <button onClick={() => updateQuantity(food.id, -1)} style={{ width: '28px', height: '28px', borderRadius: '50%', border: 'none', backgroundColor: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', boxShadow: '0 1px 2px rgba(0,0,0,0.1)' }}>
                        <Minus size={16} />
                      </button>
                      <span style={{ fontWeight: '700', fontSize: '14px', width: '16px', textAlign: 'center' }}>{inCart.quantity}</span>
                      <button onClick={() => updateQuantity(food.id, 1)} style={{ width: '28px', height: '28px', borderRadius: '50%', border: 'none', backgroundColor: '#11117F', color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' }}>
                        <Plus size={16} />
                      </button>
                    </div>
                  ) : (
                    <button 
                      onClick={() => addToCart(food)}
                      style={{ padding: '8px 16px', borderRadius: '20px', border: 'none', backgroundColor: '#11117F', color: 'white', fontWeight: '600', fontSize: '13px', display: 'flex', alignItems: 'center', gap: '6px', cursor: 'pointer' }}
                    >
                      <Plus size={16} /> Thêm
                    </button>
                  )}
                </div>
              </div>
            </div>
          );
        })}
        {activeFoods.length === 0 && (
          <p style={{ textAlign: 'center', color: '#64748B', marginTop: '20px' }}>Danh mục này chưa có món nào.</p>
        )}
      </div>

      {/* Floating Cart Bar */}
      {cart.length > 0 && (
        <div style={{ position: 'fixed', bottom: '20px', left: '20px', right: '20px', backgroundColor: '#11117F', color: 'white', borderRadius: '16px', padding: '16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', boxShadow: '0 10px 25px rgba(17, 17, 127, 0.3)', cursor: 'pointer', zIndex: 100 }} onClick={() => setShowCart(true)}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div style={{ position: 'relative' }}>
              <ShoppingCart size={24} />
              <span style={{ position: 'absolute', top: '-8px', right: '-8px', backgroundColor: '#EF4444', color: 'white', fontSize: '12px', fontWeight: '800', width: '20px', height: '20px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                {cart.reduce((sum, i) => sum + i.quantity, 0)}
              </span>
            </div>
            <div>
              <p style={{ margin: 0, fontSize: '12px', opacity: 0.8 }}>Tổng cộng</p>
              <p style={{ margin: 0, fontSize: '16px', fontWeight: '800' }}>{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(getTotal())}</p>
            </div>
          </div>
          <span style={{ fontWeight: '700', fontSize: '14px', backgroundColor: 'rgba(255,255,255,0.2)', padding: '8px 16px', borderRadius: '12px' }}>Xem giỏ hàng</span>
        </div>
      )}

      {/* Cart Drawer */}
      {showCart && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 1000, display: 'flex', flexDirection: 'column', justifyContent: 'flex-end' }}>
          <div style={{ backgroundColor: 'white', height: '85vh', borderTopLeftRadius: '24px', borderTopRightRadius: '24px', display: 'flex', flexDirection: 'column' }}>
            <div style={{ padding: '20px', borderBottom: '1px solid #E2E8F0', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h2 style={{ margin: 0, fontSize: '20px', fontWeight: '800', color: '#1E293B' }}>Giỏ hàng của bạn</h2>
              <button onClick={() => setShowCart(false)} style={{ border: 'none', background: 'none', cursor: 'pointer', color: '#64748B' }}>
                <X size={24} />
              </button>
            </div>
            
            <div style={{ flex: 1, overflowY: 'auto', padding: '20px' }}>
              {cart.map(item => (
                <div key={item.menuItemId} style={{ display: 'flex', gap: '12px', marginBottom: '20px' }}>
                  {item.image ? (
                    <img src={`http://localhost:8081${item.image}`} alt={item.foodName} style={{ width: '60px', height: '60px', borderRadius: '12px', objectFit: 'cover' }} />
                  ) : (
                    <div style={{ width: '60px', height: '60px', borderRadius: '12px', backgroundColor: '#E2E8F0' }} />
                  )}
                  <div style={{ flex: 1 }}>
                    <h4 style={{ margin: 0, fontSize: '15px', fontWeight: '700', color: '#1E293B' }}>{item.foodName}</h4>
                    <p style={{ margin: '4px 0 10px 0', fontSize: '14px', fontWeight: '800', color: '#11117F' }}>
                      {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(item.unitPrice)}
                    </p>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                      <button onClick={() => updateQuantity(item.menuItemId, -1)} style={{ width: '24px', height: '24px', borderRadius: '50%', border: '1px solid #CBD5E1', backgroundColor: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center' }}><Minus size={14} /></button>
                      <span style={{ fontWeight: '700', fontSize: '14px' }}>{item.quantity}</span>
                      <button onClick={() => updateQuantity(item.menuItemId, 1)} style={{ width: '24px', height: '24px', borderRadius: '50%', border: 'none', backgroundColor: '#11117F', color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center' }}><Plus size={14} /></button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
            
            <div style={{ padding: '20px', borderTop: '1px solid #E2E8F0', backgroundColor: '#F8FAFC' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '16px' }}>
                <span style={{ fontSize: '16px', color: '#64748B', fontWeight: '600' }}>Tổng cộng</span>
                <span style={{ fontSize: '20px', fontWeight: '800', color: '#11117F' }}>{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(getTotal())}</span>
              </div>
              <button 
                onClick={submitOrder}
                disabled={loading}
                style={{ width: '100%', padding: '16px', backgroundColor: '#10B981', color: 'white', border: 'none', borderRadius: '12px', fontSize: '16px', fontWeight: '700', opacity: loading ? 0.7 : 1 }}
              >
                {loading ? 'Đang gửi...' : 'Gửi Order (Đặt món)'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CustomerOrderApp;

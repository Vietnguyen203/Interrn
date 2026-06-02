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

  // NEW FEATURES: Search & Order Options
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedFoodForOrder, setSelectedFoodForOrder] = useState(null);
  const [orderQuantity, setOrderQuantity] = useState(1);
  const [orderOptions, setOrderOptions] = useState({
    'Ít đá': false,
    'Ít ngọt': false,
    'Không cay': false,
    'Nhiều cay': false,
    'Không hành': false
  });
  const [orderManualNote, setOrderManualNote] = useState('');

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

  const handleOpenOrderModal = (food) => {
    setSelectedFoodForOrder(food);
    setOrderQuantity(1);
    setOrderOptions({
      'Ít đá': false,
      'Ít ngọt': false,
      'Không cay': false,
      'Nhiều cay': false,
      'Không hành': false
    });
    setOrderManualNote('');
  };

  const confirmAddToCart = () => {
    if (!selectedFoodForOrder) return;
    
    const selectedOptions = Object.keys(orderOptions).filter(opt => orderOptions[opt]);
    const finalNote = [
      ...selectedOptions, 
      orderManualNote.trim()
    ].filter(Boolean).join(', ');

    setCart(prev => {
      // Find if exact same item with exact same note exists
      const existing = prev.find(item => item.menuItemId === selectedFoodForOrder.id && item.note === finalNote);
      if (existing) {
        return prev.map(item => 
          (item.menuItemId === selectedFoodForOrder.id && item.note === finalNote)
            ? { ...item, quantity: item.quantity + orderQuantity } 
            : item
        );
      }
      return [...prev, {
        cartItemId: Math.random().toString(36).substr(2, 9), // Unique ID for cart item
        menuItemId: selectedFoodForOrder.id,
        foodName: selectedFoodForOrder.foodName,
        unitPrice: selectedFoodForOrder.price,
        quantity: orderQuantity,
        note: finalNote,
        image: selectedFoodForOrder.imageUrl
      }];
    });
    
    setSelectedFoodForOrder(null);
  };

  const updateCartItemQuantity = (cartItemId, delta) => {
    setCart(prev => {
      return prev.map(item => {
        if (item.cartItemId === cartItemId) {
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

  // Filter logic: apply search query first, then active category if no search query
  let activeFoods = foods;
  if (searchQuery.trim()) {
    const q = searchQuery.toLowerCase();
    activeFoods = foods.filter(f => f.foodName.toLowerCase().includes(q));
  } else {
    activeFoods = foods.filter(f => f.categoryId === activeCategory);
  }

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

      {/* Search Bar */}
      <div style={{ padding: '16px', backgroundColor: 'white', borderBottom: '1px solid #E2E8F0' }}>
        <div style={{ display: 'flex', alignItems: 'center', backgroundColor: '#F1F5F9', borderRadius: '12px', padding: '10px 16px' }}>
          <Search size={20} color="#94A3B8" />
          <input 
            type="text" 
            placeholder="Tìm kiếm món ăn..." 
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            style={{ border: 'none', background: 'transparent', outline: 'none', width: '100%', marginLeft: '10px', fontSize: '15px', color: '#1E293B' }}
          />
          {searchQuery && (
            <button onClick={() => setSearchQuery('')} style={{ background: 'none', border: 'none', color: '#94A3B8' }}><X size={16}/></button>
          )}
        </div>
      </div>

      {/* Categories (only show when not searching) */}
      {!searchQuery.trim() && (
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
              {cat.name || cat.categoryName}
            </button>
          ))}
        </div>
      )}

      {/* Menu Items */}
      <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {activeFoods.map(food => {
          // Calculate total quantity of this food in cart (across all notes)
          const totalInCart = cart.filter(c => c.menuItemId === food.id).reduce((sum, c) => sum + c.quantity, 0);
          
          return (
            <div key={food.id} style={{ display: 'flex', backgroundColor: 'white', borderRadius: '16px', padding: '12px', gap: '12px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }} onClick={() => handleOpenOrderModal(food)}>
              {food.imageUrl ? (
                <img src={food.imageUrl.startsWith('http') ? food.imageUrl : `http://${window.location.hostname}:8081${food.imageUrl}`} alt={food.foodName} style={{ width: '90px', height: '90px', borderRadius: '12px', objectFit: 'cover' }} />
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
                  {totalInCart > 0 ? (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', backgroundColor: '#11117F', color: 'white', padding: '6px 12px', borderRadius: '20px', fontSize: '13px', fontWeight: '600' }}>
                      Đã chọn ({totalInCart})
                    </div>
                  ) : (
                    <button 
                      style={{ padding: '8px 16px', borderRadius: '20px', border: 'none', backgroundColor: '#F1F5F9', color: '#11117F', fontWeight: '700', fontSize: '13px', display: 'flex', alignItems: 'center', gap: '6px', cursor: 'pointer' }}
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
          <p style={{ textAlign: 'center', color: '#64748B', marginTop: '20px' }}>Không tìm thấy món ăn.</p>
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
              <button onClick={() => setShowCart(false)} style={{ background: 'none', border: 'none', color: '#64748B' }}><X size={24}/></button>
            </div>
            
            <div style={{ flex: 1, overflowY: 'auto', padding: '20px' }}>
              {cart.map(item => (
                <div key={item.cartItemId} style={{ display: 'flex', gap: '12px', marginBottom: '20px' }}>
                  {item.image ? (
                    <img src={item.image.startsWith('http') ? item.image : `http://${window.location.hostname}:8081${item.image}`} alt={item.foodName} style={{ width: '60px', height: '60px', borderRadius: '12px', objectFit: 'cover' }} />
                  ) : (
                    <div style={{ width: '60px', height: '60px', borderRadius: '12px', backgroundColor: '#E2E8F0' }} />
                  )}
                  <div style={{ flex: 1 }}>
                    <h3 style={{ margin: 0, fontSize: '15px', fontWeight: '700', color: '#1E293B' }}>{item.foodName}</h3>
                    <p style={{ margin: '4px 0 8px 0', fontSize: '14px', fontWeight: '700', color: '#11117F' }}>
                      {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(item.unitPrice)}
                    </p>
                    {item.note && (
                      <p style={{ margin: '0 0 8px 0', fontSize: '12px', color: '#64748B', fontStyle: 'italic', backgroundColor: '#F8FAFC', padding: '4px 8px', borderRadius: '6px' }}>Ghi chú: {item.note}</p>
                    )}
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px', backgroundColor: '#F1F5F9', padding: '4px', borderRadius: '20px', width: 'fit-content' }}>
                      <button onClick={() => updateCartItemQuantity(item.cartItemId, -1)} style={{ width: '28px', height: '28px', borderRadius: '50%', border: 'none', backgroundColor: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 1px 2px rgba(0,0,0,0.1)' }}><Minus size={16} /></button>
                      <span style={{ fontWeight: '700', fontSize: '14px', width: '20px', textAlign: 'center' }}>{item.quantity}</span>
                      <button onClick={() => updateCartItemQuantity(item.cartItemId, 1)} style={{ width: '28px', height: '28px', borderRadius: '50%', border: 'none', backgroundColor: '#11117F', color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center' }}><Plus size={16} color="white" /></button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
            
            <div style={{ padding: '20px', borderTop: '1px solid #E2E8F0', backgroundColor: 'white' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '16px' }}>
                <span style={{ fontSize: '16px', color: '#64748B', fontWeight: '600' }}>Tổng cộng</span>
                <span style={{ fontSize: '20px', fontWeight: '800', color: '#11117F' }}>{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(getTotal())}</span>
              </div>
              <button 
                onClick={submitOrder}
                disabled={loading}
                style={{ width: '100%', padding: '16px', backgroundColor: '#11117F', color: 'white', border: 'none', borderRadius: '12px', fontSize: '16px', fontWeight: '700', display: 'flex', justifyContent: 'center', alignItems: 'center' }}
              >
                {loading ? 'Đang xử lý...' : 'Xác nhận đặt món'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Order Options Popup Modal */}
      {selectedFoodForOrder && (
        <div style={{ position: 'fixed', inset: 0, zIndex: 1000, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'flex-end' }}>
          <div style={{ backgroundColor: '#FFF', width: '100%', borderTopLeftRadius: '24px', borderTopRightRadius: '24px', padding: '24px', paddingBottom: '40px', maxHeight: '90vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <h3 style={{ margin: 0, fontSize: '20px', fontWeight: '800', color: '#1E293B' }}>{selectedFoodForOrder.foodName}</h3>
              <button onClick={() => setSelectedFoodForOrder(null)} style={{ background: 'transparent', border: 'none', color: '#64748B' }}><X size={24} /></button>
            </div>
            
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
              <span style={{ fontSize: '16px', fontWeight: '600', color: '#475569' }}>Giá:</span>
              <span style={{ fontSize: '20px', fontWeight: '800', color: '#10B981' }}>{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(selectedFoodForOrder.price)}</span>
            </div>
            
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px', paddingBottom: '24px', borderBottom: '1px solid #E2E8F0' }}>
              <span style={{ fontSize: '16px', fontWeight: '600', color: '#475569' }}>Số lượng:</span>
              <div style={{ display: 'flex', alignItems: 'center', border: '1px solid #CBD5E1', borderRadius: '12px', overflow: 'hidden' }}>
                <button type="button" onClick={() => setOrderQuantity(Math.max(1, orderQuantity - 1))} style={{ width: '45px', height: '40px', border: 'none', background: '#F8FAFC', cursor: 'pointer', fontSize: '18px', fontWeight: '600' }}>-</button>
                <span style={{ width: '50px', textAlign: 'center', fontSize: '16px', fontWeight: '700' }}>{orderQuantity}</span>
                <button type="button" onClick={() => setOrderQuantity(orderQuantity + 1)} style={{ width: '45px', height: '40px', border: 'none', background: '#F8FAFC', cursor: 'pointer', fontSize: '18px', fontWeight: '600' }}>+</button>
              </div>
            </div>
            
            <div style={{ marginBottom: '24px' }}>
              <span style={{ display: 'block', fontSize: '16px', fontWeight: '700', color: '#1E293B', marginBottom: '12px' }}>Tuỳ chọn thêm (chọn nhiều)</span>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px' }}>
                {Object.keys(orderOptions).map(opt => (
                  <label key={opt} style={{ 
                    display: 'flex', alignItems: 'center', gap: '6px', 
                    padding: '10px 16px', borderRadius: '20px', 
                    border: orderOptions[opt] ? '2px solid #11117F' : '1px solid #E2E8F0',
                    backgroundColor: orderOptions[opt] ? '#EFF6FF' : '#FFF',
                    color: orderOptions[opt] ? '#11117F' : '#64748B',
                    fontWeight: orderOptions[opt] ? '700' : '500'
                  }}>
                    <input 
                      type="checkbox" 
                      checked={orderOptions[opt]} 
                      onChange={e => setOrderOptions({...orderOptions, [opt]: e.target.checked})} 
                      style={{ display: 'none' }} 
                    /> {opt}
                  </label>
                ))}
              </div>
            </div>
            
            <div style={{ marginBottom: '32px' }}>
              <span style={{ display: 'block', fontSize: '16px', fontWeight: '700', color: '#1E293B', marginBottom: '12px' }}>Ghi chú riêng cho đầu bếp</span>
              <textarea 
                value={orderManualNote}
                onChange={e => setOrderManualNote(e.target.value)}
                placeholder="Ví dụ: không lấy hành phi, cho nhiều nước dùng..."
                style={{ width: '100%', padding: '16px', borderRadius: '12px', border: '1px solid #CBD5E1', outline: 'none', resize: 'none', fontSize: '15px' }}
                rows={3}
              />
            </div>
            
            <button 
              onClick={confirmAddToCart}
              style={{ width: '100%', padding: '16px', backgroundColor: '#11117F', color: 'white', border: 'none', borderRadius: '12px', fontSize: '16px', fontWeight: '700', display: 'flex', justifyContent: 'center', alignItems: 'center' }}
            >
              Thêm vào giỏ hàng - {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(selectedFoodForOrder.price * orderQuantity)}
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default CustomerOrderApp;

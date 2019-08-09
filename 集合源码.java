ArrayList源码分析：
JDK1.7初始化出来的集合大小10

/*
如果调用空参构造器对象，初始什么数组大小为0，如果添加一个元素，默认构造出大小为10的对象数组。
如果调用非空参构造器，可以继续添加元素。
如果添加元素超过指定大小的数组，默认扩容为原来的1.5倍。最大达到int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8
怎么进行赋值数组？
怎么分析性能？
*/

public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

/**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param  initialCapacity  the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity
     *         is negative
     */
    public ArrayList(int initialCapacity) {
        if (initialCapacity > 0) {
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
            this.elementData = EMPTY_ELEMENTDATA;
        } else {
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
        }
    }
	
 /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this list
     * @throws NullPointerException if the specified collection is null
     */
	 //为什么要做深拷贝?
	 /*
			浅拷贝只是复制了对象的引用地址，两个对象指向同一个内存地址，所以修改其中任意的值，另一个值都会随之变化，这就是浅拷贝（例：assign()）

           深拷贝是将对象及值复制过来，两个对象修改其中任意的值另一个值不会改变，这就是深拷贝
	 */
    public ArrayList(Collection<? extends E> c) {
        elementData = c.toArray();
        if ((size = elementData.length) != 0) {
            // c.toArray might (incorrectly) not return Object[] (see 6260652)
            if (elementData.getClass() != Object[].class)
                elementData = Arrays.copyOf(elementData, size, Object[].class);
        } else {
            // replace with empty array.
            this.elementData = EMPTY_ELEMENTDATA;
        }
    }
	
	public boolean add(E e) {
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        elementData[size++] = e;
        return true;
    }
	
	private void ensureCapacityInternal(int minCapacity) {
        ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
    }
	
	private void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        // overflow-conscious code
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }
	
	private void ensureCapacityInternal(int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
			/**
     * Default initial capacity.
     
    private static final int DEFAULT_CAPACITY = 10;*/
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }
 
        ensureExplicitCapacity(minCapacity);
    }
	
	private void ensureCapacityInternal(int minCapacity) {
		//calculateCapacity(elementData, minCapacity)返回10，当超过10后，返回最大数值假设为11
        ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
    }
	
	private static int calculateCapacity(Object[] elementData, int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            return Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        return minCapacity;
    }
	
	 private void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        // overflow-conscious code
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }
	
	/**
     * Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
		//向右移动一位，相当除与2，当超过oldCapacity=10时候，运算结果为15。相当扩容1.5倍
        int newCapacity = oldCapacity + (oldCapacity >> 1);
		
		//初始添加元素时候自动创建数组大小为10的对象数组，
		//第一次为0-1=-1，第二次为10-11=-1
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
		
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
		//将原来的数组copy到新数组
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

/**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
	
     */
	 //数组作为一个对象，需要一定的内存存储对象头信息，对象头信息最大占用内存不可超过8字节。
https://blog.csdn.net/java_lifeng/article/details/80938123

    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
public static <T> T[] copyOf(T[] original, int newLength) {
        return (T[]) copyOf(original, newLength, original.getClass());
    }
	
public static <T,U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
        @SuppressWarnings("unchecked")
        T[] copy = ((Object)newType == (Object)Object[].class)
            ? (T[]) new Object[newLength]
			//通过调用本地方法构造出对象数组，大小为10
            : (T[]) Array.newInstance(newType.getComponentType(), newLength);
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }

Arrays.copyof(···)与System.arraycopy(···)区别：
https://blog.csdn.net/shijinupc/article/details/7827507
//复杂数据类型
public static <T,U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
        T[] copy = ((Object)newType == (Object)Object[].class)
            ? (T[]) new Object[newLength]
            : (T[]) Array.newInstance(newType.getComponentType(), newLength);
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }
public static <T> T[] copyOf(T[] original, int newLength) {
    return (T[]) copyOf(original, newLength, original.getClass());
}


LinkedList源码分析：

/**
     * Pointer to first node.
     * Invariant: (first == null && last == null) ||
     *            (first.prev == null && first.item != null)
     */
	 
    transient Node<E> first;

    /**
     * Pointer to last node.
     * Invariant: (first == null && last == null) ||
     *            (last.next == null && last.item != null)
     */
    transient Node<E> last;


//内部内只能该类自己使用，不允许被外访问
private static class Node<E> {
	//添加的元素，就是以下这个元素
        E item;
		
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }
	
public boolean add(E e) {
        linkLast(e);
        return true;
    }
	
/**
     * Links e as last element.
     */
void linkLast(E e) {
        final Node<E> l = last;
        final Node<E> newNode = new Node<>(l, e, null);
        last = newNode;
		
		//最后结点为空，即是一个空链表，没有插入任何元素
        if (l == null)
            first = newNode;
        else
            l.next = newNode;
        size++;
        modCount++;
    }
	
public void add(int index, E element) {
        checkPositionIndex(index);

        if (index == size)
            linkLast(element);
        else
            linkBefore(element, node(index));
    }
	
public boolean remove(Object o) {
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }
public E remove(int index) {
        checkElementIndex(index);
        return unlink(node(index));
    }
	
	//查看时候越界
private void checkElementIndex(int index) {
        if (!isElementIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

E unlink(Node<E> x) {
        // assert x != null;
        final E element = x.item;
        final Node<E> next = x.next;
        final Node<E> prev = x.prev;

        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            x.prev = null;
        }

        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }

        x.item = null;
        size--;
        modCount++;
        return element;
    }
	
StringBuffer源码分析：

/*调用空参构造器，初始数组容量16

StringBuffer父类AbstractStringBuilder的count，value是什么？ 调用StringBuffer对象返回什么？

append()是如何实现的？如何进行扩容？

首先判断已存字符大小+将要存入字符大小 > value.length（原来容量大小） ？
如果是原来数组大小（value.length）扩容1倍+2,利用深拷贝复制创建对象
*/

public final class StringBuffer extends AbstractStringBuilder implements java.io.Serializable, CharSequence
public StringBuffer() {
        super(16);
    }

abstract class AbstractStringBuilder implements Appendable, CharSequence {
	
	/**
     * The value is used for character storage.
     */
    char[] value;

    /**
     * The count is the number of characters used.
     */
    int count;

    /**
     * This no-arg constructor is necessary for serialization of subclasses.
     */
	
    /**
     * The value is used for character storage.
     
    char[] value;*/
AbstractStringBuilder(int capacity) {
        value = new char[capacity];
    }
@Override
    public AbstractStringBuilder append(CharSequence s) {
        if (s == null)
            return appendNull();
        if (s instanceof String)
            return this.append((String)s);
        if (s instanceof AbstractStringBuilder)
            return this.append((AbstractStringBuilder)s);

        return this.append(s, 0, s.length());
    }
@Override
    public AbstractStringBuilder append(CharSequence s, int start, int end) {
        if (s == null)
            s = "null";
        if ((start < 0) || (start > end) || (end > s.length()))
            throw new IndexOutOfBoundsException(
                "start " + start + ", end " + end + ", s.length() "
                + s.length());
				
//将要添加的字符个数				
        int len = end - start;
		
		//count为已经添加的字符个数
        ensureCapacityInternal(count + len);
        for (int i = start, j = count; i < end; i++, j++)
            value[j] = s.charAt(i);
		
		
        count += len;
        return this;
    }
	
public AbstractStringBuilder append(String str) {
        if (str == null)
            return appendNull();
        int len = str.length();
        ensureCapacityInternal(count + len);
        str.getChars(0, len, value, count);
        count += len;
        return this;
    }

private void ensureCapacityInternal(int minimumCapacity) {
        // overflow-conscious code  value.length=16
        if (minimumCapacity - value.length > 0) {
            value = Arrays.copyOf(value,
			
                    newCapacity(minimumCapacity));//获取到扩容值进行深拷贝创建出对象
        }
    }
private int newCapacity(int minCapacity) {
        // overflow-conscious code
		//扩容为原来2倍，加2
        int newCapacity = (value.length << 1) + 2;
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }
        return (newCapacity <= 0 || MAX_ARRAY_SIZE - newCapacity < 0)
            ? hugeCapacity(minCapacity)
            : newCapacity;
    }
	
	
  public StringBuffer(String str) {
        super(str.length() + 16);
        append(str);
    }
	
//添加字符时候，是线程安全的
@Override
public synchronized StringBuffer append(CharSequence s) {
        toStringCache = null;
        super.append(s);
        return this;
    }
	
@Override
    public synchronized int length() {
        return count;
    }
	
	
	
	
	
	
	
	
	
	
HashMap源码分析：
/*
为什么超过临界值后要提前扩容呢？

尽可能的出现较少的链表存储，如果避免不了链表，链表长度尽可能短，插入新元素的时候，能够尽快找到合适位置插入元素，提升性能

加载因子为什么设置为0.75？
加载因子过小数组利用率较低，加载因子过大链表出现率高，

put（）是怎么实现的？

-------》putVal():
当第一次插入元素时候，由于没有存放任何元素，调用resize（）函数，设定临界值threshold=12，创建出Node[]数组（默认大小16）。通过hash（）方法计算出Node[]下标，
并且判断下标元素及对应的链表结构是否等于了该插入结点的key值，有则替换value，没有就插入key-value。

超过临界值如何进行扩容？
超过临界值后，临界值和数组大小都变为原来的1倍。扩容后，将原来的Node数组和节点重新分配到扩容后的数据结构。

什么时候链表结构转换为红黑树？
当遍历当前的结点链表长度大于8，并且Node[]数组长度大于MIN_TREEIFY_CAPACITY = 64，则将之前的链表结构转换为红黑树。

怎么解决hash冲突的？哈希散列数据结构是什么？

HashMap的长度为什么是2^n次方？

链表是怎么转换为红黑树的？

为什么hashCode()函数相同，对象不一定相同？
每一个key值对象返回内存地址整数后，都会与当前的对象返回内存地址整数右移16位进行异或运算，很大程度使得不同的key对象得到的计算结果相同，
然而key对象不同。所以还需要使用equals()方法进行比较。
https://www.cnblogs.com/NathanYang/p/9427456.html
*/

public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
    }
public HashMap(Map<? extends K, ? extends V> m) {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        putMapEntries(m, false);
    }
	
final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
        int s = m.size();
        if (s > 0) {
            if (table == null) { // pre-size
			
			//规定临界值，当前集合的大小/负载因子+1
                float ft = ((float)s / loadFactor) + 1.0F;
				
                int t = ((ft < (float)MAXIMUM_CAPACITY) ?
                         (int)ft : MAXIMUM_CAPACITY);
						 
			 //threshold默认为0，
                if (t > threshold)
                    threshold = tableSizeFor(t);
            }
            else if (s > threshold)
                resize();
            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                putVal(hash(key), key, value, false, evict);
            }
        }
    }
/**
     * Returns a power of two size for the given target capacity.
	 返回给定目标容量的两倍幂
     */
    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
		
		//static final int MAXIMUM_CAPACITY = 1 << 30; 即2^30
		
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }
	
public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }
	

public HashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                                               initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                                               loadFactor);
        this.loadFactor = loadFactor;
        this.threshold = tableSizeFor(initialCapacity);
    }

/**
     * The default initial capacity - MUST be a power of two.
     正整型数字向左移动n位，该值变为 扩大为原来的2^n，向右移动m位数，缩小为原来的2^m
	 负整型呢？*/
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

/**
     * The load factor used when none specified in constructor.
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
/**
     * Constructs an empty <tt>HashMap</tt> with the default initial capacity
     * (16) and the default load factor (0.75).
     */
	 
	  static final int TREEIFY_THRESHOLD = 8;
	  
	  static final int MIN_TREEIFY_CAPACITY = 64;
	 
    public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
    }
	
static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        V value;
        Node<K,V> next;

        Node(int hash, K key, V value, Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey()        { return key; }
        public final V getValue()      { return value; }
        public final String toString() { return key + "=" + value; }

        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                if (Objects.equals(key, e.getKey()) &&
                    Objects.equals(value, e.getValue()))
                    return true;
            }
            return false;
        }
    }
	
public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }

static final int hash(Object key) {
        int h;
		//调用类本身的hashCode（）方法
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
	
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; Node<K,V> p; int n, i;
        if ((tab = table) == null || (n = tab.length) == 0)
			
			//当插入元素时候，初始创建出大小容量为16的Node[]数组，n=16
            n = (tab = resize()).length;
			
			//计算出数组的位置下标,确保在16范围内。如果当前数组下表元素不为空（不存有k-v），
			//将元素插入到对应下标的数组中
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
		
        else {
            Node<K,V> e; K k;
			
			//如果两个对象相同,直接赋值替换
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
            else if (p instanceof TreeNode)
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {
				
				//Node[]下标存放有链表结构，对比
                for (int binCount = 0; ; ++binCount) {
                    if ((e = p.next) == null) {
						
                        p.next = newNode(hash, key, value, null);
						
						//遍历存放Node结点的链表长度大于8
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
					
					//如果有相同的key值，跳出循环
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
					
					/*条件判断(e = p.next)和p=e,是循环链表结构逻辑*/
                }
            }
			
			，
			//将当前的Node结点旧的value值返回
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
				
				//替换旧的value值
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
				
                afterNodeAccess(e);
                return oldValue;
            }
			
        }
        ++modCount;
        if (++size > threshold)
            resize();
        afterNodeInsertion(evict);
        return null;
    }
	
final void treeifyBin(Node<K,V>[] tab, int hash) {
        int n, index; Node<K,V> e;
		
		//static final int MIN_TREEIFY_CAPACITY = 64;
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
            resize();
		
		//当Node[]数组容量大于64，改造为树形结构。之前都是链表存储的Node信息，因为存放过多的Node结点，得改造为树形结构
        else if ((e = tab[index = (n - 1) & hash]) != null) {
            TreeNode<K,V> hd = null, tl = null;
            do {
                TreeNode<K,V> p = replacementTreeNode(e, null);
                if (tl == null)
                    hd = p;
                else {
                    p.prev = tl;
                    tl.next = p;
                }
                tl = p;
            } while ((e = e.next) != null);
            if ((tab[index] = hd) != null)
                hd.treeify(tab);
        }
    }

final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
		
		//一旦其中一个 else if 语句检测为 true，其他的 else if 以及 else 语句都将跳过执行
        if (oldCap > 0) {
			
			//static final int MAXIMUM_CAPACITY = 1 << 30; 即2^30
			
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // double threshold
        }
        else if (oldThr > 0) // initial capacity was placed in threshold
            newCap = oldThr;
        else {               // zero initial threshold signifies using defaults
		
		//初始值设置为16
            newCap = DEFAULT_INITIAL_CAPACITY;
			//12
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
		
		//设定新的临界值
        threshold = newThr;
		
		//首次赋值创建Node数组大小为16
        @SuppressWarnings({"rawtypes","unchecked"})
            Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
			
        table = newTab;
		
        if (oldTab != null) {
			
			//首先遍历Node[]每一个结点
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null;
                    if (e.next == null)
                        newTab[e.hash & (newCap - 1)] = e;
                    else if (e instanceof TreeNode)
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    else { // preserve order
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        do {
                            next = e.next;
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }
	
Integer源码解析：
//https://blog.csdn.net/teacher_lee_zzsxt/article/details/79230501
// == 比较的是内存地址

    Integer i = 10;
    Integer j = 10;
    System.out.println(i == j);
      
    Integer a = 128;
    Integer b = 128;
    System.out.println(a == b);
     
    int k = 10;
    System.out.println(k == i);
    int kk = 128;
    System.out.println(kk == a);
      
    Integer m = new Integer(10);
    Integer n = new Integer(10);
    System.out.println(m == n);

反编译后：

    Integer i = Integer.valueOf(10);
    Integer j = Integer.valueOf(10);
    System.out.println(i == j);

//a,b两个不同的对象，超出[-128~127]]不会把值放入缓存中
    Integer a = Integer.valueOf(128);
    Integer b = Integer.valueOf(128);
    System.out.println(a == b);

    int k = 10;
    System.out.println(k == i.intValue());

    int kk = 128;
    System.out.println(kk == a.intValue());

    Integer m = new Integer(10);
    Integer n = new Integer(10);
    System.out.println(m == n);
打印结果为：
true
false
true
true
false

/*
int和Integer的区别？

直接声明Integer i = 10，会自动装箱变为Integer i = Integer.valueOf(10)；Integer i 会自动拆箱为 i.intValue()。

https://blog.csdn.net/earthhour/article/details/80707507
https://blog.csdn.net/maihilton/article/details/80101497
IntegerCache是怎么进行缓存的？
JVM在初始化时候，方法区的静态代码中，实例化Integer cache[],将-127~128对应的Integer对象存放其中
其中的128可以通过VM参数调整

基本数据是常量，放在方法区中，不同对象引用常量，其内存地址一样



*/
private final int value;

/**
     * A constant holding the maximum value an {@code int} can
     * have, 2<sup>31</sup>-1.
     */
@Native public static final int   MAX_VALUE = 0x7fffffff;

/**
     * A constant holding the minimum value an {@code int} can
     * have, -2<sup>31</sup>.
     */
@Native public static final int   MIN_VALUE = 0x80000000;

private static class IntegerCache {
        static final int low = -128;
        static final int high;
        static final Integer cache[];

        static {
            // high value may be configured by property
            int h = 127;
            String integerCacheHighPropValue =
                sun.misc.VM.getSavedProperty("java.lang.Integer.IntegerCache.high");
            if (integerCacheHighPropValue != null) {
                try {
                    int i = parseInt(integerCacheHighPropValue);
                    i = Math.max(i, 127);
                    // Maximum array size is Integer.MAX_VALUE
                    h = Math.min(i, Integer.MAX_VALUE - (-low) -1);
                } catch( NumberFormatException nfe) {
                    // If the property cannot be parsed into an int, ignore it.
                }
            }
            high = h;

            cache = new Integer[(high - low) + 1];
            int j = low;
			
			//实例化cache[],将-127~128对应的Integer对象存放其中
            for(int k = 0; k < cache.length; k++)
                cache[k] = new Integer(j++);

            // range [-128, 127] must be interned (JLS7 5.1.7)
            assert IntegerCache.high >= 127;
        }

        private IntegerCache() {}
    }

public Integer(int value) {
        this.value = value;
    }

public static Integer valueOf(int i) {
        if (i >= IntegerCache.low && i <= IntegerCache.high)
			
		//i值在-127~128范围，直接从cache[]中取出对象
            return IntegerCache.cache[i + (-IntegerCache.low)];
        return new Integer(i);
    }
	
public int intValue() {
        return value;
    }

public Integer(String s) throws NumberFormatException {
        this.value = parseInt(s, 10);
    }
	
public static int parseInt(String s, int radix)
                throws NumberFormatException
    {
        /*
         * WARNING: This method may be invoked early during VM initialization
         * before IntegerCache is initialized. Care must be taken to not use
         * the valueOf method.
         */

        if (s == null) {
            throw new NumberFormatException("null");
        }

        if (radix < Character.MIN_RADIX) {
            throw new NumberFormatException("radix " + radix +
                                            " less than Character.MIN_RADIX");
        }
		
//@Native public static final int   MAX_VALUE = 0x7fffffff;
        if (radix > Character.MAX_RADIX) {
            throw new NumberFormatException("radix " + radix +
                                            " greater than Character.MAX_RADIX");
        }

        int result = 0;
        boolean negative = false;
        int i = 0, len = s.length();
        int limit = -Integer.MAX_VALUE;
        int multmin;
        int digit;

        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
					
	//@Native public static final int   MIN_VALUE = 0x80000000;
					
                    limit = Integer.MIN_VALUE;
                } else if (firstChar != '+')
                    throw NumberFormatException.forInputString(s);

                if (len == 1) // Cannot have lone "+" or "-"
                    throw NumberFormatException.forInputString(s);
                i++;
            }
            multmin = limit / radix;
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(s.charAt(i++),radix);
                if (digit < 0) {
                    throw NumberFormatException.forInputString(s);
                }
                if (result < multmin) {
                    throw NumberFormatException.forInputString(s);
                }
                result *= radix;
                if (result < limit + digit) {
                    throw NumberFormatException.forInputString(s);
                }
                result -= digit;
            }
        } else {
            throw NumberFormatException.forInputString(s);
        }
        return negative ? result : -result;
    }

class String:

/** The value is used for character storage. */
    private final char value[];

public char charAt(int index) {
        if ((index < 0) || (index >= value.length)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return value[index];
    }

class Character:

 public static final int MIN_RADIX = 2;
 public static final int MAX_RADIX = 36;

public static int digit(char ch, int radix) {
        return digit((int)ch, radix);
    }
public static int digit(int codePoint, int radix) {
        return CharacterData.of(codePoint).digit(codePoint, radix);
    }

class CharacterData:
static final CharacterData of(int ch) {
        if (ch >>> 8 == 0) {     // fast-path
            return CharacterDataLatin1.instance;
        } else {
            switch(ch >>> 16) {  //plane 00-16
            case(0):
                return CharacterData00.instance;
            case(1):
                return CharacterData01.instance;
            case(2):
                return CharacterData02.instance;
            case(14):
                return CharacterData0E.instance;
            case(15):   // Private Use
            case(16):   // Private Use
                return CharacterDataPrivateUse.instance;
            default:
                return CharacterDataUndefined.instance;
            }
        }
    }
	
class class CharacterDataLatin1 extends CharacterData:

static final int A[] = new int[256];

int digit(int ch, int radix) {
        int value = -1;
		
		/* public static final int MIN_RADIX = 2;
 public static final int MAX_RADIX = 36;*/
		
        if (radix >= Character.MIN_RADIX && radix <= Character.MAX_RADIX) {
            int val = getProperties(ch);
            int kind = val & 0x1F;
            if (kind == Character.DECIMAL_DIGIT_NUMBER) {
                value = ch + ((val & 0x3E0) >> 5) & 0x1F;
            }
            else if ((val & 0xC00) == 0x00000C00) {
                // Java supradecimal digit
                value = (ch + ((val & 0x3E0) >> 5) & 0x1F) + 10;
            }
        }
        return (value < radix) ? value : -1;
    }
	
int getProperties(int ch) {
        char offset = (char)ch;
        int props = A[offset];
        return props;
    }
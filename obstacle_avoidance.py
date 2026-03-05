import rclpy
from rclpy.node import Node
from sensor_msgs.msg import LaserScan #lidardan gelen paket liste
from geometry_msgs.msg import Twist #6 adet değiken bulunan msg paketi
#twistte ilgilendiğimiz kısımlar linear.x ile angular.z olacak 

class engeldenKacma(Node):
    def __init__(self):
        super().__init__("my_smart_avoid")

        #lidardan gelecek bilgilere erişmek için /scan topici üzerinden abone oluyoruz
        #her veri tazelenmesinde callback fonksiyonumuzu çalıştırcaz  
        self.subscriber_ = self.create_subscription(LaserScan,"/scan",self.callback_kontrol,10)
        
        #hareket için tekerleklere emir vermek lazım bunun için yayın açıyoruz
        self.publisher_ = self.create_publisher(Twist,"/cmd_vel",10)
        
        self.get_logger().info("my_smart_avoid has been started.")

    #lidar verileri ile tetiklenecek callback fonksiyonu 
    def callback_kontrol(self, msg):

        #msgdan gelen 360 derecelik verinin 0 ile 20 arasını kesiyoruz ve değişkene atıyoruz
        #burada karşıya baktığımız doğru 0 doğrusudur 0 ile 20 arası sol tarafı 340 ile 360 arası sağ tarafı gösteriyor
        sol_taraf = msg.ranges[0:20] 
        #msgdan gelen 360 derecelik verinin 340 ile 360 arasını kesiyoruz ve değişkene atıyoruz
        sag_taraf = msg.ranges[340:360]

        #list comprehension 
        #sol taraf listesindeki 0.1 ile 3.5 arasındaki verileri al gerisini çöpe at
        temiz_sol = [m for m in sol_taraf if 0.1<m<3.5]
        #sağ taraf listesindeki 0.1 ile 3.5 arasındaki verileri al gerisini çöpe at
        temiz_sag = [m for m in sag_taraf if 0.1<m<3.5]

        #listenin boş olmaması için kontrol bloğu 
        #eğer liste boş geliyorsa temizleme yaparken yakında engel olmadığı için çöpe gitmiş bütün veriler
        #hata almamak için boşa giden verinin yerine 3.5 atıyoruz max olarak gözüksün diye
        if temiz_sol:
            mesafe_sol = min(temiz_sol)
        else:
            mesafe_sol = 3.5    

        #listenin boş olmaması için kontrol bloğu 
        #eğer liste boş geliyorsa temizleme yaparken yakında engel olmadığı için çöpe gitmiş bütün veriler
        #hata almamak için boşa giden verinin yerine 3.5 atıyoruz max olarak gözüksün diye
        if temiz_sag:
            mesafe_sag = min(temiz_sag)
        else:
            mesafe_sag = 3.5    

        #2 tarafta da nesne varsa birini ciddiye almamız lazım
        #min methodu ile en düşük olanı alıp en_yakin değişkeninin içine atıyoruz
        en_yakin = min(mesafe_sag,mesafe_sol)
        
        #robotun hareketini sağlayacak msg'dan sınıf oluşturduk
        emir = Twist()

        #hareket karar algoritması
        if en_yakin < 1:
            if mesafe_sag < mesafe_sol:
                emir.linear.x = 0.1 #ileri hızı azalt
                emir.angular.z = 0.5 #pozitif her zaman saat yönüdür yani sola döner
            else:
                emir.linear.x = 0.1 #ileri hızı azalt
                emir.angular.z = -0.5 #sağa dön
        else:
            emir.linear.x = 0.5 
            emir.angular.z = 0.0            

        #hareket algoritmamızdan gidecek verileri yayınlıyoruz
        self.publisher_.publish(emir)


def main(args=None):
    rclpy.init(args=args)        
    node = engeldenKacma()
    try:
        rclpy.spin(node)
    except KeyboardInterrupt:
        node.get_logger().info("my_smart_avoid has been stoped.")
    node.destroy_node()
    rclpy.shutdown()

if __name__ == "__main__":
    main()

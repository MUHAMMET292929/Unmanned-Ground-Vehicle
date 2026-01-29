#Bu kod kendi bilgisayarının terminalinde çalışacak .Ayrıca motorları tetikleyen kod Raspberry pi da çalışacak 

import socket
import pygame
import time

# --- AYARLAR ---
# BURAYA RASPBERRY PI'NIN IP ADRESİNİ YAZ 
RASPBERRY_IP = '172.20.10.12' 
PORT = 65432

# Pygame Başlat (Kumanda okumak için)
pygame.init()
pygame.joystick.init()

try:
    joystick = pygame.joystick.Joystick(0)
    joystick.init()
    print(f"Kumanda Bulundu: {joystick.get_name()}")
except:
    print("HATA: Xbox kumandası bulunamadı! Bağlı olduğundan emin ol.")
    exit()

# Socket Bağlantısı
print(f"Raspberry Pi'ye bağlanılıyor ({RASPBERRY_IP})...")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect((RASPBERRY_IP, PORT))
print("Bağlantı Başarılı! Robotu kontrol edebilirsin.")

son_komut = 'S'

def komut_gonder(komut):
    global son_komut
    if komut != son_komut: # Sadece durum değişirse veri yolla (Ağı yorma)
        client_socket.sendall(komut.encode('utf-8'))
        son_komut = komut

try:
    while True:
        pygame.event.pump() # Kumanda olaylarını yenile
        
        # Xbox Kumanda Eksenleri (Genelde Sol Analog: 1, Sağ Analog: 3 vb.)
        # Eksen değerleri -1 ile 1 arasındadır.
        y_ekseni = joystick.get_axis(1) # Sol Analog Yukarı/Aşağı
        x_ekseni = joystick.get_axis(0) # Sol Analog Sol/Sağ
        
        # Basit Kontrol Mantığı
        if y_ekseni < -0.5:   # Çubuğu ileri ittirince
            komut_gonder('F') # Forward
        elif y_ekseni > 0.5:  # Çubuğu geri çekince
            komut_gonder('B') # Back
        elif x_ekseni < -0.5: # Sola çekince
            komut_gonder('L') # Left
        elif x_ekseni > 0.5:  # Sağa çekince
            komut_gonder('R') # Right
        else:
            komut_gonder('S') # Stop (Çubuk ortadaysa)
            
        time.sleep(0.05) # İşlemciyi yormamak için minik bekleme

except KeyboardInterrupt:
    print("Kapatılıyor...")
    client_socket.close()
    pygame.quit()

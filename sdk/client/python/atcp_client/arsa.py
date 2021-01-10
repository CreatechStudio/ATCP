from Crypto.Signature import PKCS1_v1_5 as _Signature_pkcs1_v1_5
from Crypto.Cipher import PKCS1_v1_5 as _Cipher_pkcs1_v1_5
from Crypto.PublicKey import RSA as _rsa
from Crypto.Hash import SHA256 as _Hash_hash256
from Crypto import Random
import base64


class AKeyPair(object):
    def __init__(self, public_key: _rsa.RsaKey, private_key: _rsa.RsaKey):
        self.__public_key: APublicKey = APublicKey(public_key)
        self.__private_key: APrivateKey = APrivateKey(private_key)

    def get_public_key(self):
        return self.__public_key

    def get_private_key(self):
        return self.__private_key


class APublicKey(object):
    """
    公钥对象
    调用new_keys(key_length)生成或导入一个已有的bytes类型的pem格式公钥，使其成为arsa可以使用的公钥对象
    """

    def __init__(self, public_key: _rsa.RsaKey, key_length: int = 2048):
        """
        公钥对象初始化函数，
        :param public_key: 公钥的pem形式
        :param key_length: 公钥二进制长度
        """
        self.__public_key = public_key.export_key()[27: -25].replace(b'\n', b'').decode()
        self.__key_length: int = key_length
        self.__n = public_key

    def get_public_key(self) -> _rsa.RsaKey:
        """
        获取公钥对象的RSAKey
        :return: RSAKey类型公钥对象
        """
        return self.__n

    def get_key_length(self) -> int:
        """
        获取公钥二进制长度
        :return: 公钥二进制长度
        """
        return self.__key_length

    def __str__(self) -> str:
        """
        获取公钥的pem形式及二进制长度
        :return: (公钥的pem形式, 公钥二进制长度)
        """
        return self.__public_key

    @staticmethod
    def import_public_key(public_key: bytes, key_length: int):
        return APublicKey(_rsa.import_key(b'-----BEGIN PUBLIC KEY-----\n' + public_key + b'\n-----END PUBLIC KEY-----'),
                          key_length)


class APrivateKey(object):
    def __init__(self, private_key: _rsa.RsaKey, key_length: int = 2048):
        self.__private_key = private_key.export_key()[32: -30].replace(b'\n', b'').decode()
        self.__key_length: int = key_length
        self.__n = private_key

    def get_private_key(self) -> _rsa.RsaKey:
        return self.__n

    def get_key_length(self) -> int:
        return self.__key_length

    def __str__(self) -> str:
        return self.__private_key

    @staticmethod
    def import_private_key(private_key: bytes, key_length: int):
        return APrivateKey(_rsa.import_key(
            b'-----BEGIN RSA PRIVATE KEY-----\n' + private_key + b'\n-----END RSA PRIVATE KEY-----'), key_length)


def new_keys(key_length: int = 2048):
    keys = _rsa.generate(key_length, Random.new().read)
    return AKeyPair(keys.publickey(), keys)


def encrypt(content: str, public_key: APublicKey) -> bytes:
    """
    RSA加密（自动分段）
    :param content: 明文
    :param public_key: 公钥
    :return: 密文
    """
    cipher = _Cipher_pkcs1_v1_5.new(public_key.get_public_key())
    content_len = len(content)
    para_len = int(public_key.get_key_length() / 8) - 11
    # 如果长度足够短就返回加密结果
    if content_len <= para_len:
        return base64.b64encode(cipher.encrypt(content.encode('utf8')))

    # 分段加密
    offset: int = 0
    res = b''
    while content_len - offset > 0:
        res += cipher.encrypt(
            content[offset: offset + para_len if content_len - offset > para_len else None].encode('utf8'))
        offset += para_len
    return base64.b64encode(res)


def decrypt(content: base64.bytes_types, private_key: APrivateKey) -> str:
    """
    RSA解密（自动分段）
    :param content: 密文
    :param private_key: 私钥
    :return: 明文
    """
    content = base64.b64decode(content)
    cipher = _Cipher_pkcs1_v1_5.new(private_key.get_private_key())
    content_len = len(content)
    para_len = int(private_key.get_key_length() / 8)
    # 如果长度足够短就直接返回解密结果
    if content_len <= para_len:
        return cipher.decrypt(content, b'ERROR').decode()

    # 分段解密
    offset: int = 0
    res = b''
    while content_len - offset > 0:
        para = content[offset: int(offset + para_len) if content_len - offset > para_len else None]
        res += cipher.decrypt(para, b'ERROR')
        offset += para_len
    return res.decode()


def sign(content: base64.bytes_types, private_key: APrivateKey) -> bytes:
    """
    RSA签名
    :param content: 密文
    :param private_key: 私钥
    :return: 签名
    """
    return base64.b64encode(_Signature_pkcs1_v1_5.new(private_key.get_private_key()).sign(_Hash_hash256.new(content)))


def verify(content: base64.bytes_types, signature: bytes, public_key: APublicKey) -> bool:
    """
    RSA读签
    :param content: 密文
    :param signature: 签名
    :param public_key: 公钥
    :return: 如果验证成功则返回True, 反之则返回False
    """
    return _Signature_pkcs1_v1_5.new(public_key.get_public_key()).verify(_Hash_hash256.new(content),
                                                                         base64.b64decode(signature))

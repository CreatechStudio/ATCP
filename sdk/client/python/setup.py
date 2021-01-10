from setuptools import setup

setup(
    name='atcp_client',
    version='1.4.7',
    description=('A TCP long connection architecture', 'Fixed some bugs about dependency. ', 'Fixed some bugs about RSA. '),
    long_description='',
    author='ATATC',
    author_email='futerry@outlook.com',
    maintainer='ATATC',
    maintainer_email='atatc_official@aliyun.com',
    license='MIT Liscense',
    packages=['atcp_client'],
    platforms=['all'],
    include_package_data=True,
    url='https://github.com/ATATC/ATCP',
    classifiers=['Operating System :: OS Independent', 'Intended Audience :: Developers', 'License :: OSI Approved :: MIT License', 'Programming Language :: Python', 'Programming Language :: Python :: Implementation', 'Programming Language :: Python :: 2', 'Programming Language :: Python :: 2.7', 'Programming Language :: Python :: 3', 'Programming Language :: Python :: 3.4', 'Programming Language :: Python :: 3.5', 'Programming Language :: Python :: 3.6', 'Programming Language :: Python :: 3.7', 'Topic :: Software Development :: Libraries'],
    install_requires=['setuptools~=46.4.0', 'arsa~=1.1.6']
)

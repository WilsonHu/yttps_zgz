/*
SQLyog Ultimate v12.08 (64 bit)
MySQL - 5.6.22-log : Database - yttps_db
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`yttps_db` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `yttps_db`;

/*Table structure for table `user` */

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `account` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

/*Data for the table `user` */

insert  into `user`(`id`,`account`,`name`,`password`) values (1,'admin','HT','admin');

/*Table structure for table `visitor_info` */

DROP TABLE IF EXISTS `visitor_info`;

CREATE TABLE `visitor_info` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(100) NOT NULL COMMENT '姓名',
  `IdCard` varchar(18) NOT NULL COMMENT '身份证',
  `company` varchar(100) NOT NULL COMMENT '公司名称',
  `dateTime` date NOT NULL COMMENT '日期',
  `status` int(2) NOT NULL DEFAULT '0' COMMENT '是否来过，0、没来，1、来过',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8;

/*Data for the table `visitor_info` */

insert  into `visitor_info`(`id`,`name`,`IdCard`,`company`,`dateTime`,`status`) values (18,'张三','431224195510131417','汉堃','2019-03-19',1),(19,'李四','320324199308014971','汉堃','2019-03-19',1),(20,'李五','430421200002200371','依图','2019-03-18',1),(21,'王五','431023199522096210','依图','2019-03-18',0),(23,'李一','231681198610731416','汉堃','2019-03-19',0),(25,'李三','240303198603170010','依图','2019-03-19',1),(26,'王一','631681198610731416','汉堃','2019-03-19',0),(27,'王二','63022420030012421X','汉堃','2019-03-19',1),(28,'王三','640303198603170010','依图','2019-03-19',0),(29,'王四','631023199522096210','依图','2019-03-16',0),(34,'赵一','731681198610731416','汉堃','2019-03-18',0),(35,'赵二','73022420030012421X','汉堃','2019-03-18',0);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

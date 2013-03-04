package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.Setup
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author kostantinos.kougios
 *
 *         23 Jul 2012
 */
@RunWith(classOf[JUnitRunner])
class ManyToManyCompositeKeySuite extends FunSuite with ShouldMatchers
{

	val database = Setup.database
	if (database != "h2") {
		implicit val (jdbc, mapperDao, queryDao) = Setup.setupMapperDao(TypeRegistry(UserEntity, AccountEntity))

		// aliases
		val ue = UserEntity
		val ae = AccountEntity

		test("batch insert") {
			createTables()

			noise
			noise

			val acc1 = Account(1500, "Mr X1")
			val acc2 = Account(1600, "Mr X2")
			val acc3 = Account(1700, "Mr X3")

			val u1 = User("ref1", "user X", Set(acc1, acc2))
			val u2 = User("ref2", "user Y", Set(acc3, acc2))
			val List(i1, i2) = mapperDao.insertBatch(UserEntity, List(u1, u2))
			i1 should be(u1)
			i2 should be(u2)

			mapperDao.select(UserEntity, (i1.id, i1.reference)).get should be(i1)
			mapperDao.select(UserEntity, (i2.id, i2.reference)).get should be(i2)
		}

		test("batch update on inserted") {
			createTables()

			noise
			noise

			val acc1 = Account(1500, "Mr X1")
			val acc2 = Account(1600, "Mr X2")
			val acc3 = Account(1700, "Mr X3")
			val acc4 = Account(1800, "Mr X4")

			val user1 = User("ref1", "user X", Set(acc1, acc2))
			val user2 = User("ref2", "user Y", Set(acc3, acc2))
			val List(i1, i2) = mapperDao.insertBatch(UserEntity, List(user1, user2)).map {
				inserted =>
					mapperDao.update(UserEntity, inserted, inserted.copy(accounts = inserted.accounts - acc2 + acc4))
			}

			mapperDao.select(UserEntity, (i1.id, i1.reference)).get should be(i1)
			mapperDao.select(UserEntity, (i2.id, i2.reference)).get should be(i2)
		}

		test("batch update on selected") {
			createTables()

			noise
			noise

			val acc1 = Account(1500, "Mr X1")
			val acc2 = Account(1600, "Mr X2")
			val acc3 = Account(1700, "Mr X3")
			val acc4 = Account(1800, "Mr X4")

			val user1 = User("ref1", "user X", Set(acc1, acc2))
			val user2 = User("ref2", "user Y", Set(acc3, acc2))
			val List(i1, i2) = mapperDao.insertBatch(UserEntity, List(user1, user2)).map {
				inserted =>
					mapperDao.update(UserEntity, inserted, inserted.copy(accounts = inserted.accounts - acc2 + acc4))
			}.map {
				updated =>
					mapperDao.select(UserEntity, (updated.id, updated.reference)).get
			}

			mapperDao.select(UserEntity, (i1.id, i1.reference)).get should be(i1)
			mapperDao.select(UserEntity, (i2.id, i2.reference)).get should be(i2)
		}

		test("query") {
			createTables()

			// add some noise
			noise
			noise

			// and now the real thing

			val inserted1 = mapperDao.insert(UserEntity, User("ref1", "user X1", Set(Account(1500, "Mr XA"), Account(1600, "Mr XA"))))
			val inserted2 = mapperDao.insert(UserEntity, User("ref2", "user X2", Set(Account(1500, "Mr XB"), Account(1700, "Mr XB"))))

			import Query._

			(select
				from ue
				join(ue, ue.accounts, ae)
				where ae.serial === 1500
				).toList.toSet should be === Set(inserted1, inserted2)

			(select
				from ue
				join(ue, ue.accounts, ae)
				where ae.serial === 1700
				).toList.toSet should be === Set(inserted2)
		}

		test("insert, select and delete") {
			createTables()

			noise
			noise

			val acc1 = Account(1500, "Mr X1")
			val acc2 = Account(1600, "Mr X2")

			val u = User("ref1", "user X", Set(acc1, acc2))
			val inserted = mapperDao.insert(UserEntity, u)
			inserted should be === u

			val selected = mapperDao.select(UserEntity, (inserted.id, inserted.reference)).get
			selected should be === inserted

			mapperDao.delete(UserEntity, selected)

			mapperDao.select(UserEntity, (inserted.id, inserted.reference)) should be(None)
		}

		test("update, remove") {
			createTables()

			// add some noise
			noise
			noise

			// and now the real thing
			val acc1 = Account(1500, "Mr X1")
			val acc2 = Account(1600, "Mr X2")

			val inserted = mapperDao.insert(UserEntity, User("ref1", "user X", Set(acc1, acc2)))
			val upd = inserted.copy(accounts = inserted.accounts.filterNot(_ == acc2))
			val updated = mapperDao.update(UserEntity, inserted, upd)
			updated should be === upd

			mapperDao.select(UserEntity, (inserted.id, inserted.reference)).get should be === updated
		}

		test("update, add") {
			createTables()

			// add some noise
			noise
			noise

			// and now the real thing
			val acc1 = Account(1500, "Mr X1")
			val acc2 = Account(1600, "Mr X2")

			val inserted = mapperDao.insert(UserEntity, User("ref1", "user X", Set(acc1)))
			val upd = inserted.copy(accounts = Set(acc1, acc2))
			val updated = mapperDao.update(UserEntity, inserted, upd)
			updated should be === upd

			mapperDao.select(UserEntity, (updated.id, updated.reference)).get should be === updated
		}

		def noise = mapperDao.insert(UserEntity, User("refX", "user X", Set(Account(50, "Noise1"), Account(51, "Noise2"), Account(52, "Noise3"))))
		def createTables() = {
			Setup.dropAllTables(jdbc)
			Setup.queries(this, jdbc).update("ddl")
			if (Setup.database == "oracle") {
				Setup.createSeq(jdbc, "UserSeq")
				Setup.createSeq(jdbc, "account_seq")
			}
		}
	}

	case class User(reference: String, name: String, accounts: Set[Account])

	case class Account(serial: Long, name: String)

	// we need to take special care for oracle which doesn't
	// seem to like "User" table (quoted or not).
	object UserEntity extends Entity[(Int, String),SurrogateIntAndNaturalStringId, User](if (database == "oracle") "UserX" else "User")
	{
		val id = key("id") sequence (
			if (database == "oracle") Some("UserSeq") else None
			) autogenerated (_.id)
		val reference = key("reference") to (_.reference)
		val name = column("name") to (_.name)
		val accounts = manytomany(AccountEntity) join("User_Account", "user_id" :: "user_reference" :: Nil, "account_id" :: "account_serial" :: Nil) to (_.accounts)

		def constructor(implicit m) = new User(reference, name, accounts) with Stored
		{
			val id: Int = UserEntity.id
		}
	}

	object AccountEntity extends Entity[(Int, Long),SurrogateIntAndNaturalLongId, Account]
	{
		val id = key("id") sequence (
			if (database == "oracle") Some("account_seq") else None
			) autogenerated (_.id)
		val serial = key("serial") to (_.serial)
		val name = column("name") to (_.name)

		def constructor(implicit m) = new Account(serial, name) with Stored
		{
			val id: Int = AccountEntity.id
		}
	}

}

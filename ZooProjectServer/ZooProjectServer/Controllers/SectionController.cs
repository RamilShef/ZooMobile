using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using System.Data;

namespace ZooProjectServer.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class SectionController : ControllerBase
    {
        private readonly IConfiguration _configuration;

        public SectionController(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        // GET: api/section/{id}
        [HttpGet("{id}")]
        public ActionResult<Section> GetSection(int id)
        {
            var section = new Section();
            using (var connection = new SqlConnection(_configuration.GetConnectionString("ZooDbConnection")))
            {
                connection.Open();
                var command = new SqlCommand("SELECT * FROM Section WHERE id = @id", connection);
                command.Parameters.AddWithValue("@id", id);

                using (var reader = command.ExecuteReader())
                {
                    if (reader.Read())
                    {
                        section.Id = reader.GetInt32("id");
                        section.SectionName = reader.GetString("SectionName");
                        section.Description = reader.GetString("Description");
                        return Ok(section);
                    }
                }
            }
            return NotFound();
        }

        // GET: api/section
        [HttpGet]
        public ActionResult<List<Section>> GetAllSections()
        {
            var sections = new List<Section>();
            using (var connection = new SqlConnection(_configuration.GetConnectionString("ZooDbConnection")))
            {
                connection.Open();
                var command = new SqlCommand("SELECT * FROM Section", connection);

                using (var reader = command.ExecuteReader())
                {
                    while (reader.Read())
                    {
                        sections.Add(new Section
                        {
                            Id = reader.GetInt32("id"),
                            SectionName = reader.GetString("SectionName"),
                            Description = reader.GetString("Description")
                        });
                    }
                }
            }
            return Ok(sections);
        }
    }
}